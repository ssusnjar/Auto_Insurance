import { use, useState } from "react";
import "./App.css";
import { PrimeReactProvider } from "primereact/api";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Chart } from "primereact/chart";

export default function App() {
  const [value, setValue] = useState<string>("");

  return (
    <PrimeReactProvider>
      <div className="app">
        <div className="sidebar">
          <Button
            label="Novi upit"
            icon="pi pi-plus"
            size="small"
            className="no-style-button"
          />

          <div className="history">
            <h3>
              <div
                className="pi pi-history
"
              ></div>{" "}
              Povijest
            </h3>
            <Button label="Upit 2" size="small" className="no-style-button" />
            <Button label="Upit 1" size="small" className="no-style-button" />
          </div>
        </div>
        <div className="container">
          <div>
            <InputText
              value={value}
              placeholder="Postavi pitanje"
              onChange={(e) => setValue(e.target.value)}
            />
            <Button icon="pi pi-send" />
          </div>
        </div>
      </div>
    </PrimeReactProvider>
  );
}
