//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : Header:
//*
//*
//*  Purpose :
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*
//*
//****************************************************************************

package com.wolery
package fx
package dock

//****************************************************************************

sealed abstract class Position

object Position
{
  case object CENTER   extends Position
  case object LEFT     extends Position
  case object RIGHT    extends Position
  case object TOP      extends Position
  case object BOTTOM   extends Position
}

//****************************************************************************
