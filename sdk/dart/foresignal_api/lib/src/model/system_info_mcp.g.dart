// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_mcp.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoMcpCWProxy {
  SystemInfoMcp enabled(bool? enabled);

  SystemInfoMcp sessions(int? sessions);

  SystemInfoMcp transports(SystemInfoMcpTransports? transports);

  SystemInfoMcp tools(int? tools);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoMcp(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoMcp(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoMcp call({
    bool? enabled,
    int? sessions,
    SystemInfoMcpTransports? transports,
    int? tools,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoMcp.copyWith(...)` or call `instanceOfSystemInfoMcp.copyWith.fieldName(value)` for a single field.
class _$SystemInfoMcpCWProxyImpl implements _$SystemInfoMcpCWProxy {
  const _$SystemInfoMcpCWProxyImpl(this._value);

  final SystemInfoMcp _value;

  @override
  SystemInfoMcp enabled(bool? enabled) => call(enabled: enabled);

  @override
  SystemInfoMcp sessions(int? sessions) => call(sessions: sessions);

  @override
  SystemInfoMcp transports(SystemInfoMcpTransports? transports) =>
      call(transports: transports);

  @override
  SystemInfoMcp tools(int? tools) => call(tools: tools);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoMcp(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoMcp(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoMcp call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? sessions = const $CopyWithPlaceholder(),
    Object? transports = const $CopyWithPlaceholder(),
    Object? tools = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoMcp(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      sessions: sessions == const $CopyWithPlaceholder()
          ? _value.sessions
          // ignore: cast_nullable_to_non_nullable
          : sessions as int?,
      transports: transports == const $CopyWithPlaceholder()
          ? _value.transports
          // ignore: cast_nullable_to_non_nullable
          : transports as SystemInfoMcpTransports?,
      tools: tools == const $CopyWithPlaceholder()
          ? _value.tools
          // ignore: cast_nullable_to_non_nullable
          : tools as int?,
    );
  }
}

extension $SystemInfoMcpCopyWith on SystemInfoMcp {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoMcp.copyWith(...)` or `instanceOfSystemInfoMcp.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoMcpCWProxy get copyWith => _$SystemInfoMcpCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoMcp _$SystemInfoMcpFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoMcp', json, ($checkedConvert) {
      final val = SystemInfoMcp(
        enabled: $checkedConvert('enabled', (v) => v as bool?),
        sessions: $checkedConvert('sessions', (v) => (v as num?)?.toInt()),
        transports: $checkedConvert(
          'transports',
          (v) => v == null
              ? null
              : SystemInfoMcpTransports.fromJson(v as Map<String, dynamic>),
        ),
        tools: $checkedConvert('tools', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoMcpToJson(SystemInfoMcp instance) =>
    <String, dynamic>{
      'enabled': ?instance.enabled,
      'sessions': ?instance.sessions,
      'transports': ?instance.transports?.toJson(),
      'tools': ?instance.tools,
    };
