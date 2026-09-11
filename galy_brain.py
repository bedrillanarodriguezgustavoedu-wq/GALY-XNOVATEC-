from fastapi import FastAPI, Query
import mysql.connector
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import random

app = FastAPI()

# Permitir conexiones desde la App
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Conexión a tu XAMPP
def get_db_connection():
    return mysql.connector.connect(
        host="localhost",
        user="root",
        password="",
        database="galy"
    )

@app.get("/chat")
def chat(message: str = Query(...)):
    msg = message.lower()
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    reply = ""

    # --- INTELIGENCIA DE BÚSQUEDA GALY AI ---

    # 1. Búsqueda por Marca Específica
    marcas = ["apple", "sony", "samsung", "logitech", "asus", "hp", "msi", "lenovo", "acer"]
    marca_encontrada = next((m for m in marcas if m in msg), None)

    if "mejor" in msg or "recomendacion" in msg or "recomienda" in msg:
        if marca_encontrada:
            cursor.execute(f"SELECT nombre, precio_oferta FROM productos WHERE marca LIKE '{marca_encontrada}' ORDER BY calificacion DESC LIMIT 1")
            res = cursor.fetchone()
            if res:
                reply = f"Como experto de GALY, mi mejor recomendación de {marca_encontrada.upper()} es la {res['nombre']}. Tiene una calificacion perfecta y está a solo S/ {res['precio_oferta']}."
        else:
            cursor.execute("SELECT nombre, precio_oferta, marca FROM productos ORDER BY calificacion DESC, precio_oferta DESC LIMIT 1")
            res = cursor.fetchone()
            if res:
                reply = f"¡Sin duda! El producto estrella ahora mismo es la {res['nombre']} de {res['marca']}. Es potencia pura para lo que necesites."

    # 2. Búsqueda por Presupuesto
    elif "barato" in msg or "economico" in msg or "oferta" in msg:
        cursor.execute("SELECT nombre, precio_oferta FROM productos WHERE descuento > 20 ORDER BY precio_oferta ASC LIMIT 1")
        res = cursor.fetchone()
        if res:
            reply = f"¡Tengo una súper oferta! La {res['nombre']} tiene más del 20% de descuento y te sale a S/ {res['precio_oferta']}. ¡Vuela que se agota!"

    # 3. Búsqueda por Tipo de Uso
    elif "gamer" in msg or "gaming" in msg or "juego" in msg:
        cursor.execute("SELECT nombre, precio_oferta FROM productos WHERE categoria_nombre = 'Laptops' AND (nombre LIKE '%Gaming%' OR nombre LIKE '%Nitro%' OR nombre LIKE '%TUF%') LIMIT 1")
        res = cursor.fetchone()
        if res:
            reply = f"¡Para gaming no hay pierde! Te sugiero la {res['nombre']}. Corre todos los juegos actuales en ultra y está a S/ {res['precio_oferta']}."

    # 4. Saludos y Ayuda General
    elif "hola" in msg or "quien eres" in msg:
        reply = "¡Hola! Soy el cerebro de GALY AI. Conozco cada uno de nuestros 100 productos a la perfección. ¿Buscas alguna marca o presupuesto específico?"

    # 5. Fallback Inteligente (Busca cualquier coincidencia en el nombre)
    if not reply:
        cursor.execute(f"SELECT nombre, precio_oferta FROM productos WHERE nombre LIKE '%{msg.split()[-1]}%' LIMIT 1")
        res = cursor.fetchone()
        if res:
            reply = f"He encontrado algo relacionado: {res['nombre']} a S/ {res['precio_oferta']}. ¿Te gustaría saber más de este modelo?"
        else:
            reply = "Estoy analizando nuestro inventario... ¿Podrías darme más detalles? Busco por marcas, precios o si lo quieres para estudio, trabajo o gaming."

    cursor.close()
    conn.close()
    return {"reply": reply}

if __name__ == "__main__":
    print("🚀 GALY AI BRAIN v2.0 (Expert Edition) ACTIVO")
    uvicorn.run(app, host="0.0.0.0", port=8000)
