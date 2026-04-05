import tkinter as tk
from tkinter import ttk
from tkinter import messagebox

class MainWindow:
    def __init__(self, controller):
        self.controller = controller
        self.controller.register_window(self)
        self.root = tk.Tk()
        self.root.title("Cacophony™ Client - © Louis SWINNEN 2026")
        self.root.config(bd=5)
        self.silent_dialogs = False
        self.host = tk.StringVar()
        self.passw = tk.StringVar()
        self.login = tk.StringVar()
        self.tls = tk.BooleanVar()
        self.port = tk.IntVar()
        self.message = tk.StringVar()
        self.draw_window()

    def start_main_loop(self):
        self.tf_host.focus()
        self.root.mainloop()

    def draw_window(self):
        self.draw_top_pane()
        self.draw_center_pane()
        self.draw_footer_pane()
        self.not_connected_mode()

    def set_window_title(self, suffix):
        base_title = "Cacophony™ Client - © Louis SWINNEN 2026"
        if suffix and suffix.strip():
            self.root.title(f"{base_title} [{suffix}]")
        else:
            self.root.title(base_title)

    def set_geometry(self, geometry):
        if geometry and geometry.strip():
            self.root.geometry(geometry)

    def set_silent_dialogs(self, enabled):
        self.silent_dialogs = enabled

    def schedule(self, delay_ms, callback):
        self.root.after(delay_ms, callback)

    def draw_top_pane(self):
        top_pane = ttk.LabelFrame(self.root, padding=(3, 3, 12, 12), text=" Connection to Cacophony™ Node ")
        top_pane.pack(fill=tk.X)
        top_pane.columnconfigure(1, weight=1)

        ttk.Label(top_pane, text="Host:").grid(row=0, column=0, sticky=tk.E)
        self.tf_host = ttk.Entry(top_pane, textvariable=self.host)
        self.tf_host.grid(row=0, column=1, sticky=tk.EW, padx=5, pady=5)

        ttk.Label(top_pane, text="Port:").grid(row=0, column=2, sticky=tk.E)
        self.tf_port = ttk.Entry(top_pane, textvariable=self.port)
        self.tf_port.grid(row=0, column=3, sticky=tk.EW, padx=5, pady=2)

        ttk.Label(top_pane, text="Login:").grid(row=1, column=0, sticky=tk.E)
        self.tf_login = ttk.Entry(top_pane, textvariable=self.login)
        self.tf_login.grid(row=1,column=1, sticky=tk.EW, padx=5, pady=5)

        ttk.Label(top_pane, text="Password:").grid(row=1, column=2, sticky=tk.E)
        self.tf_password = ttk.Entry(top_pane, show="*", textvariable=self.passw)
        self.tf_password.grid(row=1,column=3, sticky=tk.EW, padx=5, pady=2)

        inner_frame = ttk.Frame(top_pane)
        inner_frame.grid(row=2, column=0, columnspan=4, sticky=tk.NSEW)
        inner_frame.columnconfigure(1,weight=1)

        ttk.Label(inner_frame, text="Security:").grid(row=0, column=0, sticky=tk.E)
        self.cb_tls = ttk.Checkbutton(inner_frame, text="with SSL/TLS", variable=self.tls)
        self.cb_tls.grid(row=0, column=1, padx=5, pady=2, sticky=tk.W)
        self.bt_onboarding = ttk.Button(inner_frame, text="Onboarding", command=self.onboarding)
        self.bt_onboarding.grid(row=0, column=2, padx=5, pady=2,sticky=tk.E)
        self.bt_authenticate = ttk.Button(inner_frame, text="Authenticate", command=self.authenticate)
        self.bt_authenticate.grid(row=0, column=3, padx=5, pady=2, sticky=tk.E)

    def draw_center_pane(self):
        title_pane = ttk.Frame(self.root, padding=(5, 0, 5, 0))
        ttk.Label(title_pane, text="Messages:").grid(row=0, column=0, sticky=tk.E)
        title_pane.pack(fill=tk.X)
        center_pane = ttk.Frame(self.root, padding=(5, 0, 5, 0))
        center_pane.pack(fill=tk.BOTH, expand=True)

        self.text_box = tk.Text(center_pane,  wrap = "word")
        ys = ttk.Scrollbar(center_pane, orient = 'vertical', command = self.text_box.yview)
        xs = ttk.Scrollbar(center_pane, orient = 'horizontal', command = self.text_box.xview)
        self.text_box['yscrollcommand'] = ys.set
        self.text_box['xscrollcommand'] = xs.set
        self.text_box.tag_configure('highlightline', background='yellow', font='TkFixedFont', relief='raised')
        self.text_box.grid(column = 0, row = 0, sticky = 'nwes')
        self.text_box["state"] = "disabled"
        xs.grid(column = 0, row = 1, sticky = 'we')
        ys.grid(column = 1, row = 0, sticky = 'ns')
        center_pane.grid_columnconfigure(0, weight = 1)
        center_pane.grid_rowconfigure(0, weight = 1)

        bottom_pane = ttk.Frame(self.root, padding=(5, 0, 5, 0))
        bottom_pane.columnconfigure(1, weight=1)
        self.lb_message = ttk.Label(bottom_pane, text="Your message:")
        self.lb_message.grid(row=0, column=0, padx=10, pady=2, sticky=tk.E)
        self.tf_message = ttk.Entry(bottom_pane, textvariable=self.message)
        self.tf_message.grid(row=0, column=1, sticky=tk.EW, padx=5, pady=2)
        self.bt_send = ttk.Button(bottom_pane, text="Send Message", command=self.send_message)
        self.bt_send.grid(row=0, column=2, padx=10, pady=2,sticky=tk.EW)     
        self.bt_subscribe = ttk.Button(bottom_pane, text="Subscribe", command=self.subscribe)
        self.bt_subscribe.grid(row=0, column=3, padx=10, pady=2,sticky=tk.E)     
        bottom_pane.pack(fill=tk.X)

    def draw_footer_pane(self):
        footer_pane = ttk.Frame(self.root, padding=(5,10,5,0))
        footer_pane.pack(fill=tk.X)
        footer_pane.columnconfigure(1, weight=1)
        self.bt_about = ttk.Button(footer_pane, text="About", command=self.about)
        self.bt_about.grid(row=0, column=0, pady=3, sticky=tk.E)
        self.bt_quit = ttk.Button(footer_pane, text="Quit", command=self.quit)
        self.bt_quit.grid(row=0, column=1, pady=3, sticky=tk.E)

    def not_connected_mode(self):
        self.tf_message.state(["disabled"])
        self.bt_send.state(["disabled"])
        self.bt_subscribe.state(["disabled"])
    
    def connected_mode(self):
        self.tf_host.state(["disabled"])
        self.tf_port.state(["disabled"])
        self.tf_login.state(["disabled"])
        self.tf_password.state(["disabled"])
        self.bt_onboarding.state(["disabled"])
        self.bt_authenticate.state(["disabled"])
        self.cb_tls.state(["disabled"])
        self.tf_message.state(["!disabled"])
        self.bt_send.state(["!disabled"])
        self.bt_subscribe.state(["!disabled"])

    def quit(self):
        self.root.destroy()
        self.controller.on_quit()
    
    def about(self):
        pass

    def subscribe(self):
        subscriber = self.message.get()
        if subscriber == None or len(subscriber.strip()) == 0:
            self.show_message("Mention user of tag to subscribe", True)
        else:
            self.controller.on_subscribe(subscriber)
            self.message.set("")

    def send_message(self):
        message = self.message.get()
        if message == None or len(message.strip()) == 0:
            self.show_message("Message is missing", True)
        else:
            self.controller.on_send(message)
            self.message.set("")

    def no_action(self):
        pass

    def add_message(self, message, is_colored):
        self.text_box["state"] = "normal"
        if is_colored == True:
            self.text_box.insert('end', f"{message}\n", 'highlightline')
        else:
            self.text_box.insert('end', f"{message}\n")
        self.text_box.see("end")
        self.text_box["state"] = "disabled"
    
    def show_message(self, message, is_error):
        if self.silent_dialogs and not is_error:
            self.add_message(f"[info] {message}", False)
        elif is_error:
            messagebox.showerror("Error", message)
        else:
            messagebox.showinfo("Information", message)
    
    def authenticate(self):
        if self.check_before_authenticate():
            print("[MainWindow] Calling MainWindowController::on_authenticate()")
            self.controller.on_authenticate(self.host.get(), int(self.port.get()), self.login.get(), self.passw.get(), self.tls.get())
        else:
            self.show_message("All fields are mandatory!\nPort is numeric (1025-65535)", True)

    def onboarding(self):
        if self.check_before_authenticate():
            print("[MainWindow] Calling MainWindowController::on_onboarding()")
            self.controller.on_onboarding(self.host.get(), int(self.port.get()), self.login.get(), self.passw.get(), self.tls.get())
        else:
            self.show_message("All fields are mandatory!\nPort is numeric (1025-65535)", True)

    def check_before_authenticate(self):
        try:
            host = self.host.get()
            port = int(self.port.get())
            login = self.login.get()
            passw = self.passw.get()
            tls = self.tls.get()
            if(not(host and host.strip())) or port <= 1024 or port >= 65536 or not (login and login.strip()) or not(passw and passw.strip()):
                return False
            else:
                return True
        except ValueError:
            return False
