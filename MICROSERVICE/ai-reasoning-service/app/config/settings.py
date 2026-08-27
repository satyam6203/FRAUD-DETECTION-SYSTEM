import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    gemini_api_key: str = os.getenv("GEMINI_API_KEY", "")
    gemini_model: str = "gemini-3.6-flash"
    postgres_url: str = os.getenv(
        "DATABASE_URL",
        "postgresql://neondb_owner:npg_I1biAcv6DjXh@ep-curly-shadow-axy1e595-pooler.c-4.us-east-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
    )
    payment_service_url: str = os.getenv(
        "PAYMENT_SERVICE_URL",
        "http://localhost:8081"
    )
    fraud_service_url: str = os.getenv(
        "FRAUD_SERVICE_URL",
        "http://localhost:8082"
    )

settings = Settings()