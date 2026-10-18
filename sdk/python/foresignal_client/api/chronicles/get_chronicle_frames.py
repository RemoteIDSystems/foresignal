import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_frame_index import ChronicleFrameIndex
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    chronicle_id: str,
    *,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    step: str | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    params["step"] = step

    json_layers: list[str] | Unset = UNSET
    if not isinstance(layers, Unset):
        json_layers = layers

    params["layers"] = json_layers

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicles/{chronicle_id}/frames".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChronicleFrameIndex | Problem | None:
    if response.status_code == 200:
        response_200 = ChronicleFrameIndex.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ChronicleFrameIndex | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    step: str | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> Response[ChronicleFrameIndex | Problem]:
    """Playback frame index

     Counts and changed entities per step over chronicle storage.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        step (str | Unset):  Example: PT5M.
        layers (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleFrameIndex | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        from_=from_,
        to=to,
        step=step,
        layers=layers,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    step: str | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> ChronicleFrameIndex | Problem | None:
    """Playback frame index

     Counts and changed entities per step over chronicle storage.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        step (str | Unset):  Example: PT5M.
        layers (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleFrameIndex | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
        from_=from_,
        to=to,
        step=step,
        layers=layers,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    step: str | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> Response[ChronicleFrameIndex | Problem]:
    """Playback frame index

     Counts and changed entities per step over chronicle storage.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        step (str | Unset):  Example: PT5M.
        layers (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleFrameIndex | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        from_=from_,
        to=to,
        step=step,
        layers=layers,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    step: str | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> ChronicleFrameIndex | Problem | None:
    """Playback frame index

     Counts and changed entities per step over chronicle storage.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        step (str | Unset):  Example: PT5M.
        layers (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleFrameIndex | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
            from_=from_,
            to=to,
            step=step,
            layers=layers,
        )
    ).parsed
