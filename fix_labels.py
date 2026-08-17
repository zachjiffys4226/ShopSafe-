import os
import re

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Find placeholder = { Text("...") } and add label = { Text("...") }
    def repl(m):
        text = m.group(1)
        return f'label = {{ Text("{text}") }},\n{m.group(0)}'
        
    new_content = re.sub(r'([ \t]*)placeholder = \{ Text\("([^"]+)"\) \},', r'\1label = { Text("\2") },\n\1placeholder = { Text("\2") },', content)
    
    with open(filepath, 'w') as f:
        f.write(new_content)

fix_file("app/src/main/java/com/example/shopsafe/ui/screens/LandingScreen.kt")
fix_file("app/src/main/java/com/example/shopsafe/ui/screens/AuthModal.kt")
