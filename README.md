# Messages

Kafka -> /turtle1/cmd_vel -> /turtlesim

```
cmd_vel:
    linear:
        x: 1.0 # Wie viel vorwärts. <0 -> rückwärts, >0 -> vorwärts. Aus Sicht des Roboters. 
        y: 0.0
        z: 0.0
    angular:
        x: 0.0
        y: 0.0
        z: 0.0 # Wie viel drehen. <0 -> links, >0 -> rechts

pose:
    x: 5.544444561  # Globale Position. links Unten 0,0. Rechts unten 11,0
    y: 5.544444561
    theta: 0.0  # Ausrichtung in radianten. 0 -> nach rechts, pi/2 (90) -> nach oben, pi (180) -> nach links, 3pi/2 (270) -> nach unten
    linear_velocity: 2.0 # Wie schnell fährt der Roboter. <0 -> rückwärts, >0 -> vorwärts
    angular_velocity: 2.0 # Wie schnell dreht sich der Roboter. <0 -> links, >0 -> rechts
```

Input: Globale Sicht
Output: Lokale Sicht
