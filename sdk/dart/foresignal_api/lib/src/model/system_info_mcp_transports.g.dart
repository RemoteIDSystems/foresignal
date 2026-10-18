// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_mcp_transports.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoMcpTransportsCWProxy {
  SystemInfoMcpTransports streamable(bool? streamable);

  SystemInfoMcpTransports legacySse(bool? legacySse);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoMcpTransports(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoMcpTransports(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoMcpTransports call({bool? streamable, bool? legacySse});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoMcpTransports.copyWith(...)` or call `instanceOfSystemInfoMcpTransports.copyWith.fieldName(value)` for a single field.
class _$SystemInfoMcpTransportsCWProxyImpl
    implements _$SystemInfoMcpTransportsCWProxy {
  const _$SystemInfoMcpTransportsCWProxyImpl(this._value);

  final SystemInfoMcpTransports _value;

  @override
  SystemInfoMcpTransports streamable(bool? streamable) =>
      call(streamable: streamable);

  @override
  SystemInfoMcpTransports legacySse(bool? legacySse) =>
      call(legacySse: legacySse);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoMcpTransports(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoMcpTransports(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoMcpTransports call({
    Object? streamable = const $CopyWithPlaceholder(),
    Object? legacySse = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoMcpTransports(
      streamable: streamable == const $CopyWithPlaceholder()
          ? _value.streamable
          // ignore: cast_nullable_to_non_nullable
          : streamable as bool?,
      legacySse: legacySse == const $CopyWithPlaceholder()
          ? _value.legacySse
          // ignore: cast_nullable_to_non_nullable
          : legacySse as bool?,
    );
  }
}

extension $SystemInfoMcpTransportsCopyWith on SystemInfoMcpTransports {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoMcpTransports.copyWith(...)` or `instanceOfSystemInfoMcpTransports.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoMcpTransportsCWProxy get copyWith =>
      _$SystemInfoMcpTransportsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoMcpTransports _$SystemInfoMcpTransportsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemInfoMcpTransports', json, ($checkedConvert) {
  final val = SystemInfoMcpTransports(
    streamable: $checkedConvert('streamable', (v) => v as bool?),
    legacySse: $checkedConvert('legacySse', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$SystemInfoMcpTransportsToJson(
  SystemInfoMcpTransports instance,
) => <String, dynamic>{
  'streamable': ?instance.streamable,
  'legacySse': ?instance.legacySse,
};
