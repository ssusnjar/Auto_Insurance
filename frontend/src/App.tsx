import { useState, useEffect } from "react";
import "./App.css";
import { PrimeReactProvider } from "primereact/api";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Chart } from "primereact/chart";

export default function App() {
  const [value, setValue] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const [chartData, setChartData] = useState({});

  const handleClick = () => {
    setLoading(true);

    fetch("http://localhost:3001/departments")
      .then((res) => res.json())
      .then((data) => {
        setChartData({
          labels: data.map((el: any) => el.label),
          datasets: [
            {
              data: data.map((el: any) => el.value),
            },
          ],
        });

        setLoading(false);
      });
  };

  const chartOptions = {
    plugins: {
      legend: {
        position: "bottom",
        labels: {
          usePointStyle: true,
        },
      },
    },
  };

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
          <div className="data-view">
            {loading ? (
              <div>loading..</div>
            ) : (
              <>
                {chartData?.datasets && chartData.datasets.length > 0 && (
                  <Chart type="pie" data={chartData} options={chartOptions} />
                )}
              </>
            )}

            <div className="input-section">
              <InputText
                value={value}
                placeholder="Postavi pitanje"
                onChange={(e) => setValue(e.target.value)}
              />
              <Button icon="pi pi-send" onClick={handleClick} />
            </div>
          </div>
        </div>
      </div>
    </PrimeReactProvider>
  );
}
