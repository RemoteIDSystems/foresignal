// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_sinks.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoSinksCWProxy {
  SystemInfoSinks enabled(int? enabled);

  SystemInfoSinks failing(int? failing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoSinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoSinks(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoSinks call({int? enabled, int? failing});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoSinks.copyWith(...)` or call `instanceOfSystemInfoSinks.copyWith.fieldName(value)` for a single field.
class _$SystemInfoSinksCWProxyImpl implements _$SystemInfoSinksCWProxy {
  const _$SystemInfoSinksCWProxyImpl(this._value);

  final SystemInfoSinks _value;

  @override
  SystemInfoSinks enabled(int? enabled) => call(enabled: enabled);

  @override
  SystemInfoSinks failing(int? failing) => call(failing: failing);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoSinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoSinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoSinks call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? failing = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoSinks(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as int?,
      failing: failing == const $CopyWithPlaceholder()
          ? _value.failing
          // ignore: cast_nullable_to_non_nullable
          : failing as int?,
    );
  }
}

extension $SystemInfoSinksCopyWith on SystemInfoSinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoSinks.copyWith(...)` or `instanceOfSystemInfoSinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoSinksCWProxy get copyWith => _$SystemInfoSinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoSinks _$SystemInfoSinksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoSinks', json, ($checkedConvert) {
      final val = SystemInfoSinks(
        enabled: $checkedConvert('enabled', (v) => (v as num?)?.toInt()),
        failing: $checkedConvert('failing', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoSinksToJson(SystemInfoSinks instance) =>
    <String, dynamic>{
      'enabled': ?instance.enabled,
      'failing': ?instance.failing,
    };
