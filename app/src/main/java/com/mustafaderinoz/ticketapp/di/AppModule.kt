package com.mustafaderinoz.ticketapp.di

import com.mustafaderinoz.ticketapp.viewmodel.LoginViewModel
import com.mustafaderinoz.ticketapp.viewmodel.RegisterViewModel
import com.mustafaderinoz.ticketapp.viewmodel.HomeViewModel
import com.mustafaderinoz.ticketapp.viewmodel.TicketDetailViewModel
import com.mustafaderinoz.ticketapp.viewmodel.EventDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::TicketDetailViewModel)
    viewModelOf(::EventDetailViewModel)
}