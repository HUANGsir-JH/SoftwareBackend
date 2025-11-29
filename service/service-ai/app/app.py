from typing import Any, Optional
from fastapi import FastAPI
from pydantic import BaseModel
from service.agent import SearchAgentState

app = FastAPI()

class Response(BaseModel):
    code: int
    msg: str
    data: Optional[Any] | None

    @classmethod
    def success(cls, data: Any = None) -> "Response":
        return cls(code=200, msg="success", data=data)
    
    @classmethod
    def error(cls, msg: str = "error", code: int = 500) -> "Response":
        return cls(code=code, msg=msg, data=None)


@app.get("/ai/search")
def search_with_ai(query: str) -> Response:
    state = SearchAgentState(
        question=query,
        retrieved_notes=[],
        final_answer=""
    )

    return Response.success(data=state["final_answer"])