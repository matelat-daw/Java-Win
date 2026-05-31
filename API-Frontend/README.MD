- [API](#api)
La API usa el puerto 8080 y el usuario root sin contraseña para conectarse a la Base de Datos.
Incluye el DUMP techsotore.sql para importar la base de datos que incluye dos tablas, una producto y una categoría que están relacionadas y ya contienen 5 categorías y 6 productos.
La vista de contacto utiliza mi correo electrónico para enviar los mensajes a la tienda, y se responde al usuario con un E-mail que avisa que se le contactará a la brevedad. En application.properties está mi Clave de Paso para enviar E-mail desde mi cuenta de matelat@gmail.com.
La tienda soporta productos con imagen, las imagenes se guardan en la carpeta del usuario dentro de la carpeta tienda-api/imgs, incluida en el proyecto, es necesario copiar la carpeta tienda-api y pegarla en C:\Usuarios\<Nombre del Usuario Logueado>
- [Frontend](#frontend)
El frontend muetra una página de inicio con 4 enlaces a las vistas, el primero es a la propia página de inicio, el segundo a la página de productos, el tercero a la página de resultados/resumen y el cuarto a la página de contacto.
En la vista de productos se muestra una lista de productos con su nombre, precio, cantidad en stock, activo o no e imagen.Se puede Agregar Producto Nuevo, visualizar los detalles de un producto, editar los datos, cambiar la imagen y eliminar el producto. El CRUD Completo.
El frontend puede funcionar desde localhost, servidor Apache, Nginx o desde localhost:5500 Live Server, extensión de Visual Studio Code. El back end tiene una Clase de Configuracion para CORS.