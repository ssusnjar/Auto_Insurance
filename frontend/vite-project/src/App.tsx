import { useState } from "react";
import "./App.css";
import { PrimeReactProvider } from "primereact/api";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";

export default function App() {
  const [value, setValue] = useState<string>("");
  return (
    <PrimeReactProvider>
      <div className="app">
        <div className="sidebar">
          <Button label="New Chat" icon="pi pi-plus" size="small" />
        </div>
        <div className="container">
          <div>
            <InputText
              value={value}
              placeholder="Postavi pitanje"
              onChange={(e) => setValue(e.target.value)}
            />
            <Button
              icon="pi pi-send
"
            />
          </div>
        </div>
      </div>
    </PrimeReactProvider>
  );
}
