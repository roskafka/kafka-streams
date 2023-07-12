#!/bin/bash

function help() {
  echo "Usage: ./run_simulation.sh app [options]"
  echo "Arguments:"
  echo "  app: the application to run. The number is an alias and is the order in which the applications should be started."
  echo "    kafka, 0: run kafka and and schema registry"
  echo "    bridge, 1: run turtlesim and roskafka bridge"
  echo "    control, 2: run robot control nodes"
  echo "    stream, 3: run kafka streams"
  echo "    register, 4: register robots"
  echo "Options:"
  echo "  --help: show this help message"
  echo "  --debug: run roskafka bridge in debug mode"
  echo "Examples:"
  echo "  ./run_simulation.sh bridge"
  echo "  ./run_simulation.sh 1"
  echo "  ./run_simulation.sh 1 --debug"
  exit 0
}

function kafka() {
    cd ~/kafka-streams || exit
    docker compose up -d
}

# app: bridge as function
function bridge() {
  cd ~/ros2_ws || exit

  # start turtlesim and store the process id
  ros2 run turtlesim turtlesim_node &
  turtlesim_pid=$!

  # catch ctrl-c and kill turtlesim
  trap 'kill $turtlesim_pid' EXIT

  # catch any other error and kill turtlesim
  trap 'kill $turtlesim_pid' ERR

  # kill default turtle
  ros2 service call /kill turtlesim/Kill "{name: turtle1}"

  # spawn new turtles
  ros2 service call /spawn turtlesim/srv/Spawn "{x: 2.0, y: 3.0, theta: 0.0, name: 'klaus'}"
  ros2 service call /spawn turtlesim/srv/Spawn "{x: 4.0, y: 4.0, theta: 0.0, name: 'dieter'}"

  # launch roskafka bridge
  if [ "$2" == "--debug" ]; then
    ros2 launch roskafka roskafka.launch.yaml debug:=true
  else
    ros2 launch roskafka roskafka.launch.yaml
  fi

  # kill turtlesim
  kill $turtlesim_pid
}

function control() {
  cd ~/ros2_ws/src/robot_controll/robot_controll || exit
  python3 movement_commands.py &
  movement_commands_pid=$!
  trap 'kill $movement_commands_pid' EXIT
  trap 'kill $movement_commands_pid' ERR

  python3 background_color_listener.py
}

function register() {
  cd ~/ros2_ws/src/robot_registration/robot_registration || exit
  python3 robot_registration_publisher.py <<EOF
turtlesim/klaus
turtlesim/dieter
EOF
}

function stream() {
  cd ~/kafka-streams || exit
  ./mvnw compile quarkus:dev
}


# check for help argument
if [ "$1" == "--help" ]; then
  help
fi

# parse arguments
if [ "$1" == "kafka" ] || [ "$1" == "0" ]; then
  kafka
elif [ "$1" == "bridge" ] || [ "$1" == "1" ]; then
  bridge "$2"
elif [ "$1" == "control" ] || [ "$1" == "2" ]; then
  control
elif [ "$1" == "stream" ] || [ "$1" == "3" ]; then
  stream
elif [ "$1" == "register" ] || [ "$1" == "4" ]; then
  register
else
  echo "Invalid argument: $1"
  help
fi
