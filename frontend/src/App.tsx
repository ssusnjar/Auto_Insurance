import { useState } from "react";
import "./App.css";
import { PrimeReactProvider } from "primereact/api";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Chart } from "primereact/chart";
import { Divider } from "primereact/divider";
import { Card } from "primereact/card";

interface History {
  id: number;
  input: string;
}

export default function App() {
  const [value, setValue] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const [chartData, setChartData] = useState({});
  const [history, setHistory] = useState<History[]>([]);
  const [historyId, setHistoryId] = useState<number>(1);

  const handleClick = () => {
    setLoading(true);
    fetch("http://localhost:8080/api/v1/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: "Pokaži kako se krece cijena u odnosu na godine",
      }),
    })
      .then((res) => res.json())
      .then((data) => {
        console.log("data", data);
        setChartData({
          labels: data.map((el: any) => el.label),
          datasets: [
            {
              data: data.salaries.map((el: any) => el.value),
            },
          ],
        });

        setChartData(newData);
        setHistory((prev) => [...prev, { id: historyId, input: value }]);
        setHistoryId(historyId + 1);
        setLoading(false);
        setValue("");
      });
  };

  const handleHistoryClick = (input: string) => {
    fetch(`http://localhost:3001/citiesData/${input}`)
      .then((res) => res.json())
      .then((data) => {
        const newData = {
          labels: data.salaries.map((el: any) => el.label),
          id: data.salaries.map((el: any) => el.id),
          datasets: [
            {
              data: data.salaries.map((el: any) => el.value),
            },
          ],
        };

        setChartData(newData);
        setValue("");
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
  };

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
            <h3>
              <div
                className="pi pi-history
"
              ></div>{" "}
              Povijest
            </h3>
            {history.map((item, index) => {
              return (
                <Button
                  key={index}
                  label={item.input}
                  value={item.input}
                  size="small"
                  className="no-style-button"
                  onClick={() => handleHistoryClick(item.input)}
                />
              );
            })}
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
