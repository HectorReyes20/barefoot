// Validar que las contraseñas coincidan
    document.querySelector('form').addEventListener('submit', function(e) {
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (password !== confirmPassword) {
            e.preventDefault();
            alert('Las contraseñas no coinciden');
            document.getElementById('confirmPassword').focus();
        }
    });

    // Mostrar fortaleza de contraseña
    document.getElementById('password').addEventListener('input', function() {
        const password = this.value;
        let strength = 0;

        if (password.length >= 6) strength++;
        if (password.length >= 10) strength++;
        if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
        if (/\d/.test(password)) strength++;
        if (/[^a-zA-Z\d]/.test(password)) strength++;

        const small = this.nextElementSibling;
        if (strength < 2) {
            small.textContent = 'Contraseña débil';
            small.className = 'text-danger small';
        } else if (strength < 4) {
            small.textContent = 'Contraseña media';
            small.className = 'text-warning small';
        } else {
            small.textContent = 'Contraseña fuerte';
            small.className = 'text-success small';
        }
    });