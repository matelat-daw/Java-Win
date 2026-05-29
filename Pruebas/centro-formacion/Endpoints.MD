# 📚 API Documentation: Gestión de Alumnos y Cursos

## 🎓 Módulo de Alumnos

<p>Controlador asignado: <code>AlumnoController</code></p>

<table style="width: 100%; border-collapse: collapse; margin: 15px 0; font-family: sans-serif; box-shadow: 0 4px 6px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden;">
  <thead>
    <tr style="background-color: #2c3e50; color: #ffffff; text-align: left; font-weight: bold;">
      <th style="padding: 12px 15px; width: 15%;">Método</th>
      <th style="padding: 12px 15px; width: 35%;">Ruta</th>
      <th style="padding: 12px 15px; width: 50%;">Descripción</th>
    </tr>
  </thead>
  <tbody>
    <tr style="border-bottom: 1px solid #dddddd;">
      <td style="padding: 12px 15px;"><span style="background-color: #2ecc71; color: white; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 0.85em;">GET</span></td>
      <td style="padding: 12px 15px;"><code>/api/v1/alumnos</code></td>
      <td style="padding: 12px 15px;">Devuelve listado de alumnos <code>(id, nombre, email)</code>.</td>
    </tr>
    <tr style="border-bottom: 1px solid #dddddd; background-color: #f9f9f9;">
      <td style="padding: 12px 15px;"><span style="background-color: #3498db; color: white; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 0.85em;">GET</span></td>
      <td style="padding: 12px 15px;"><code>/api/v1/alumnos/en-cursos</code></td>
      <td style="padding: 12px 15px;">Devuelve alumnos detallados junto con sus cursos inscritos.</td>
    </tr>
    <tr style="border-bottom: 2px solid #2c3e50;">
      <td style="padding: 12px 15px;"><span style="background-color: #e74c3c; color: white; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 0.85em;">DELETE</span></td>
      <td style="padding: 12px 15px;"><code>/api/v1/alumnos/{id}</code></td>
      <td style="padding: 12px 15px;">Elimina el alumno registrado con el ID especificado.</td>
    </tr>
  </tbody>
</table>

## 💻 Módulo de Cursos

<p>Controlador asignado: <code>CursoController</code></p>

<table style="width: 100%; border-collapse: collapse; margin: 15px 0; font-family: sans-serif; box-shadow: 0 4px 6px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden;">
  <thead>
    <tr style="background-color: #34495e; color: #ffffff; text-align: left; font-weight: bold;">
      <th style="padding: 12px 15px; width: 15%;">Método</th>
      <th style="padding: 12px 15px; width: 35%;">Ruta</th>
      <th style="padding: 12px 15px; width: 50%;">Descripción</th>
    </tr>
  </thead>
  <tbody>
    <tr style="border-bottom: 1px solid #dddddd;">
      <td style="padding: 12px 15px;"><span style="background-color: #2ecc71; color: white; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 0.85em;">GET</span></td>
      <td style="padding: 12px 15px;"><code>/api/v1/cursos</code></td>
      <td style="padding: 12px 15px;">Devuelve listado de cursos <code>(id, titulo, modalidad, duracion)</code>.</td>
    </tr>
    <tr style="border-bottom: 1px solid #dddddd; background-color: #f9f9f9;">
      <td style="padding: 12px 15px;"><span style="background-color: #3498db; color: white; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 0.85em;">GET</span></td>
      <td style="padding: 12px 15px;"><code>/api/v1/cursos/con-alumnos</code></td>
      <td style="padding: 12px 15px;">Devuelve cursos detallados junto con sus alumnos inscritos.</td>
    </tr>
    <tr style="border-bottom: 2px solid #34495e;">
      <td style="padding: 12px 15px;"><span style="background-color: #e74c3c; color: white; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 0.85em;">DELETE</span></td>
      <td style="padding: 12px 15px;"><code>/api/v1/cursos/{id}</code></td>
      <td style="padding: 12px 15px;">Elimina el curso registrado con el ID especificado.</td>
    </tr>
  </tbody>
</table>