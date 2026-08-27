from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routes import reasoning_routes
from app.config.settings import settings
import uvicorn

app = FastAPI(
    title="AI Reasoning Service",
    description="LangChain + LangGraph powered fraud analysis service",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(
    reasoning_routes.router,
    prefix="/api/v1/ai",
    tags=["AI Reasoning"]
)

@app.get("/health")
def health():
    return {"status": "UP", "service": "ai-reasoning-service"}

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8084, reload=True)