from pydantic import BaseModel


class PayloadPosition(BaseModel):
    x: float
    y: float
    theta: float
    linear_velocity: float
    angular_velocity: float


class MetaData(BaseModel):
    robot: str
    topic: str
    type: str


class Message(BaseModel):
    payload: PayloadPosition
    meta: MetaData


class Vector3(BaseModel):
    x: float
    y: float
    z: float


class VelocityCommand(BaseModel):
    linear: Vector3
    angular: Vector3
