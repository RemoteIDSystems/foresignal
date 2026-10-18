from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.plugin import Plugin
from ...models.plugin_update import PluginUpdate
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    name: str,
    *,
    body: PluginUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/plugins/{name}".format(
            name=quote(str(name), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Plugin | Problem | None:
    if response.status_code == 200:
        response_200 = Plugin.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Plugin | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    name: str,
    *,
    client: AuthenticatedClient,
    body: PluginUpdate,
) -> Response[Plugin | Problem]:
    """Enable or disable a plugin

     Enables a plugin after the admin reviewed its sha256 (must match the file), or disables it. ABI v1
    parsers run in Chicory with no WASI imports, 64 MB memory, 5 s timeout and 16 MB output. Enabling
    needs plugins on (`409 PLUGINS_DISABLED`), `sha256` in the body (`400`), a `plugin.wasm` that hashes
    to it and to the hash `plugin.json` declares (`409 PLUGIN_HASH_MISMATCH`), and a module that loads:
    valid WebAssembly, importing nothing but `env.foresignal_log(i32, i32, i32)` and exporting `memory`,
    `alloc`, `dealloc` and `foresignal_parse` (`400`). The approval is bound to that hash: if the file
    later changes, the plugin stops running (state `hash-mismatch`) until it is reviewed and enabled
    again. Disabling always works, also while plugins are off.

    **Scope:** `admin`.

    Args:
        name (str):
        body (PluginUpdate): Enable or disable a plugin after reviewing its sha256

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Plugin | Problem]
    """

    kwargs = _get_kwargs(
        name=name,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    name: str,
    *,
    client: AuthenticatedClient,
    body: PluginUpdate,
) -> Plugin | Problem | None:
    """Enable or disable a plugin

     Enables a plugin after the admin reviewed its sha256 (must match the file), or disables it. ABI v1
    parsers run in Chicory with no WASI imports, 64 MB memory, 5 s timeout and 16 MB output. Enabling
    needs plugins on (`409 PLUGINS_DISABLED`), `sha256` in the body (`400`), a `plugin.wasm` that hashes
    to it and to the hash `plugin.json` declares (`409 PLUGIN_HASH_MISMATCH`), and a module that loads:
    valid WebAssembly, importing nothing but `env.foresignal_log(i32, i32, i32)` and exporting `memory`,
    `alloc`, `dealloc` and `foresignal_parse` (`400`). The approval is bound to that hash: if the file
    later changes, the plugin stops running (state `hash-mismatch`) until it is reviewed and enabled
    again. Disabling always works, also while plugins are off.

    **Scope:** `admin`.

    Args:
        name (str):
        body (PluginUpdate): Enable or disable a plugin after reviewing its sha256

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Plugin | Problem
    """

    return sync_detailed(
        name=name,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    name: str,
    *,
    client: AuthenticatedClient,
    body: PluginUpdate,
) -> Response[Plugin | Problem]:
    """Enable or disable a plugin

     Enables a plugin after the admin reviewed its sha256 (must match the file), or disables it. ABI v1
    parsers run in Chicory with no WASI imports, 64 MB memory, 5 s timeout and 16 MB output. Enabling
    needs plugins on (`409 PLUGINS_DISABLED`), `sha256` in the body (`400`), a `plugin.wasm` that hashes
    to it and to the hash `plugin.json` declares (`409 PLUGIN_HASH_MISMATCH`), and a module that loads:
    valid WebAssembly, importing nothing but `env.foresignal_log(i32, i32, i32)` and exporting `memory`,
    `alloc`, `dealloc` and `foresignal_parse` (`400`). The approval is bound to that hash: if the file
    later changes, the plugin stops running (state `hash-mismatch`) until it is reviewed and enabled
    again. Disabling always works, also while plugins are off.

    **Scope:** `admin`.

    Args:
        name (str):
        body (PluginUpdate): Enable or disable a plugin after reviewing its sha256

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Plugin | Problem]
    """

    kwargs = _get_kwargs(
        name=name,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    name: str,
    *,
    client: AuthenticatedClient,
    body: PluginUpdate,
) -> Plugin | Problem | None:
    """Enable or disable a plugin

     Enables a plugin after the admin reviewed its sha256 (must match the file), or disables it. ABI v1
    parsers run in Chicory with no WASI imports, 64 MB memory, 5 s timeout and 16 MB output. Enabling
    needs plugins on (`409 PLUGINS_DISABLED`), `sha256` in the body (`400`), a `plugin.wasm` that hashes
    to it and to the hash `plugin.json` declares (`409 PLUGIN_HASH_MISMATCH`), and a module that loads:
    valid WebAssembly, importing nothing but `env.foresignal_log(i32, i32, i32)` and exporting `memory`,
    `alloc`, `dealloc` and `foresignal_parse` (`400`). The approval is bound to that hash: if the file
    later changes, the plugin stops running (state `hash-mismatch`) until it is reviewed and enabled
    again. Disabling always works, also while plugins are off.

    **Scope:** `admin`.

    Args:
        name (str):
        body (PluginUpdate): Enable or disable a plugin after reviewing its sha256

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Plugin | Problem
    """

    return (
        await asyncio_detailed(
            name=name,
            client=client,
            body=body,
        )
    ).parsed
