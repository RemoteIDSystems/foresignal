from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.custom_source_created import CustomSourceCreated
from ...models.custom_source_input import CustomSourceInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: CustomSourceInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/custom-sources",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CustomSourceCreated | Problem | None:
    if response.status_code == 201:
        response_201 = CustomSourceCreated.from_dict(response.json())

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
) -> Response[CustomSourceCreated | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: CustomSourceInput,
) -> Response[CustomSourceCreated | Problem]:
    """Create a custom push source

     Registers a runtime PUSH source (STANDARD tier, `custom=true`) after registry validation and
    `IngestPolicyValidator`; id unique across static and dynamic sources (409). Returns the ingest key
    (`eik_` + 32 random bytes) exactly once. `profile: personal-location` applies the 039 defaults.
    `preset` (059) fills every member the input leaves out from a MappingPreset, with `presetParams`
    (`lon`/`lat` rounded to 0.01° unless `exactLocation`; `tombstoneOnResolve` for alertmanager), and
    the response's `setup` carries the device instructions with the concrete URL (it may contain the
    key; shown once). `mapping` is compiled against the input's kinds and observedAtMode (400
    MAPPING_INVALID). `licenceTerms` (109) sets the structured terms of the pushed data: each PUSH batch
    snapshots their id on its fetch row and every egress path enforces them.

    **Scope:** `admin`.

    Args:
        body (CustomSourceInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CustomSourceCreated | Problem]
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
    body: CustomSourceInput,
) -> CustomSourceCreated | Problem | None:
    """Create a custom push source

     Registers a runtime PUSH source (STANDARD tier, `custom=true`) after registry validation and
    `IngestPolicyValidator`; id unique across static and dynamic sources (409). Returns the ingest key
    (`eik_` + 32 random bytes) exactly once. `profile: personal-location` applies the 039 defaults.
    `preset` (059) fills every member the input leaves out from a MappingPreset, with `presetParams`
    (`lon`/`lat` rounded to 0.01° unless `exactLocation`; `tombstoneOnResolve` for alertmanager), and
    the response's `setup` carries the device instructions with the concrete URL (it may contain the
    key; shown once). `mapping` is compiled against the input's kinds and observedAtMode (400
    MAPPING_INVALID). `licenceTerms` (109) sets the structured terms of the pushed data: each PUSH batch
    snapshots their id on its fetch row and every egress path enforces them.

    **Scope:** `admin`.

    Args:
        body (CustomSourceInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CustomSourceCreated | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: CustomSourceInput,
) -> Response[CustomSourceCreated | Problem]:
    """Create a custom push source

     Registers a runtime PUSH source (STANDARD tier, `custom=true`) after registry validation and
    `IngestPolicyValidator`; id unique across static and dynamic sources (409). Returns the ingest key
    (`eik_` + 32 random bytes) exactly once. `profile: personal-location` applies the 039 defaults.
    `preset` (059) fills every member the input leaves out from a MappingPreset, with `presetParams`
    (`lon`/`lat` rounded to 0.01° unless `exactLocation`; `tombstoneOnResolve` for alertmanager), and
    the response's `setup` carries the device instructions with the concrete URL (it may contain the
    key; shown once). `mapping` is compiled against the input's kinds and observedAtMode (400
    MAPPING_INVALID). `licenceTerms` (109) sets the structured terms of the pushed data: each PUSH batch
    snapshots their id on its fetch row and every egress path enforces them.

    **Scope:** `admin`.

    Args:
        body (CustomSourceInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CustomSourceCreated | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: CustomSourceInput,
) -> CustomSourceCreated | Problem | None:
    """Create a custom push source

     Registers a runtime PUSH source (STANDARD tier, `custom=true`) after registry validation and
    `IngestPolicyValidator`; id unique across static and dynamic sources (409). Returns the ingest key
    (`eik_` + 32 random bytes) exactly once. `profile: personal-location` applies the 039 defaults.
    `preset` (059) fills every member the input leaves out from a MappingPreset, with `presetParams`
    (`lon`/`lat` rounded to 0.01° unless `exactLocation`; `tombstoneOnResolve` for alertmanager), and
    the response's `setup` carries the device instructions with the concrete URL (it may contain the
    key; shown once). `mapping` is compiled against the input's kinds and observedAtMode (400
    MAPPING_INVALID). `licenceTerms` (109) sets the structured terms of the pushed data: each PUSH batch
    snapshots their id on its fetch row and every egress path enforces them.

    **Scope:** `admin`.

    Args:
        body (CustomSourceInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CustomSourceCreated | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
