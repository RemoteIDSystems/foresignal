from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.exercise_run import ExerciseRun
from ...models.exercise_run_create import ExerciseRunCreate
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ExerciseRunCreate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/exercises",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExerciseRun | Problem | None:
    if response.status_code == 200:
        response_200 = ExerciseRun.from_dict(response.json())

        return response_200

    if response.status_code == 201:
        response_201 = ExerciseRun.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExerciseRun | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ExerciseRunCreate,
) -> Response[ExerciseRun | Problem]:
    """Start an exercise replay

     Replays a retained window through the selected rules in run-local state (cooldown, rate cap, active
    windows) and delivers matches as `[EXERCISE]` alerts only to channels with acceptExercise=true or
    named in the run; real alert state and statistics are never touched. `dryRun=true` returns projected
    counts without delivering. Window at most `exercise.maxWindow` (7 d); one run at a time. Errors:
    `400 REPLAY_BEYOND_RETENTION` (names the source and its earliest replayable time), `409
    EXERCISE_RUNNING`. Audited.

    **Scope:** `agent`.

    Args:
        body (ExerciseRunCreate): Create and start an exercise replay, or project it with dryRun
            (115)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExerciseRun | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ExerciseRunCreate,
) -> ExerciseRun | Problem | None:
    """Start an exercise replay

     Replays a retained window through the selected rules in run-local state (cooldown, rate cap, active
    windows) and delivers matches as `[EXERCISE]` alerts only to channels with acceptExercise=true or
    named in the run; real alert state and statistics are never touched. `dryRun=true` returns projected
    counts without delivering. Window at most `exercise.maxWindow` (7 d); one run at a time. Errors:
    `400 REPLAY_BEYOND_RETENTION` (names the source and its earliest replayable time), `409
    EXERCISE_RUNNING`. Audited.

    **Scope:** `agent`.

    Args:
        body (ExerciseRunCreate): Create and start an exercise replay, or project it with dryRun
            (115)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExerciseRun | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ExerciseRunCreate,
) -> Response[ExerciseRun | Problem]:
    """Start an exercise replay

     Replays a retained window through the selected rules in run-local state (cooldown, rate cap, active
    windows) and delivers matches as `[EXERCISE]` alerts only to channels with acceptExercise=true or
    named in the run; real alert state and statistics are never touched. `dryRun=true` returns projected
    counts without delivering. Window at most `exercise.maxWindow` (7 d); one run at a time. Errors:
    `400 REPLAY_BEYOND_RETENTION` (names the source and its earliest replayable time), `409
    EXERCISE_RUNNING`. Audited.

    **Scope:** `agent`.

    Args:
        body (ExerciseRunCreate): Create and start an exercise replay, or project it with dryRun
            (115)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExerciseRun | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ExerciseRunCreate,
) -> ExerciseRun | Problem | None:
    """Start an exercise replay

     Replays a retained window through the selected rules in run-local state (cooldown, rate cap, active
    windows) and delivers matches as `[EXERCISE]` alerts only to channels with acceptExercise=true or
    named in the run; real alert state and statistics are never touched. `dryRun=true` returns projected
    counts without delivering. Window at most `exercise.maxWindow` (7 d); one run at a time. Errors:
    `400 REPLAY_BEYOND_RETENTION` (names the source and its earliest replayable time), `409
    EXERCISE_RUNNING`. Audited.

    **Scope:** `agent`.

    Args:
        body (ExerciseRunCreate): Create and start an exercise replay, or project it with dryRun
            (115)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExerciseRun | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
