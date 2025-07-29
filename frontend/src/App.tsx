import { useState } from "react";
import "./App.css";
import { PrimeReactProvider } from "primereact/api";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Chart } from "primereact/chart";
import { Divider } from "primereact/divider";
import { Card } from "primereact/card";

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

  const startNewChat = () => {
    setValue("");
    setLoading(false);
    setChartData("");
  }

  return (
    <PrimeReactProvider>
      <div className="app">
        <div className="sidebar">
          <div className="chat-section">  
            <Button
              label="Novi upit"
              icon="pi pi-plus"
              size="small"
              className="p-button-sm p-button-text w-full mb-2"
              onClick={startNewChat} 
            />
            <Divider />
            <div className="history">
                <h4><i className="pi pi-history" />Povijest</h4>
                <Button label="Upit 2" className="p-button-text p-button-sm w-full" />
                <Button label="Upit 1" className="p-button-text p-button-sm w-full" />
            </div>
          </div>
        </div>
        <div className="container">
          <div className="data-view">
            {loading ? (
              <div>loading..</div>
            ) : (
              <>
                {chartData?.datasets && chartData.datasets.length > 0 && (
                  <Card className="chart-box">
                  <Chart type="pie" data={chartData} options={chartOptions} />
                  </Card>
                )}
              </>
            )}

            <div className="input-section">
              <InputText
                value={value}
                placeholder="Postavi pitanje"
                onChange={(e) => setValue(e.target.value)}
                className="w-20rem"
              />
              <Button icon="pi pi-send" onClick={handleClick} />
            </div>
          </div>
        </div>
      </div>
    </PrimeReactProvider>
  );
}
