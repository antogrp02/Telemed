# Manual smoke tests

## Chat message rendering
1. Accedi come paziente e apri la pagina Chat & Televisita (`/patient/chat`).
2. Invia un messaggio che contenga un URL (es. `https://example.com`): verifica che il link sia cliccabile e apra una nuova scheda.
3. Invia un messaggio con caratteri da eseguire escaping (es. `<script>` e `"quote"`): verifica che vengano mostrati come testo e non eseguiti.
4. Ripeti le stesse verifiche nella pagina chat del medico (`/doctor/chat`).
