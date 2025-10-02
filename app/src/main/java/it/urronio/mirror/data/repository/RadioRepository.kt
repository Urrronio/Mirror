package it.urronio.mirror.data.repository

import it.urronio.mirror.data.model.Radio

interface RadioRepository {
    fun getAttachedRadios() : List<Radio>
}