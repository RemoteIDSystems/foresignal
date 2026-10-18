// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_warnings_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoWarningsInnerCWProxy {
  SystemInfoWarningsInner code(String code);

  SystemInfoWarningsInner severity(
    SystemInfoWarningsInnerSeverityEnum? severity,
  );

  SystemInfoWarningsInner message(String message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoWarningsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoWarningsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoWarningsInner call({
    String code,
    SystemInfoWarningsInnerSeverityEnum? severity,
    String message,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoWarningsInner.copyWith(...)` or call `instanceOfSystemInfoWarningsInner.copyWith.fieldName(value)` for a single field.
class _$SystemInfoWarningsInnerCWProxyImpl
    implements _$SystemInfoWarningsInnerCWProxy {
  const _$SystemInfoWarningsInnerCWProxyImpl(this._value);

  final SystemInfoWarningsInner _value;

  @override
  SystemInfoWarningsInner code(String code) => call(code: code);

  @override
  SystemInfoWarningsInner severity(
    SystemInfoWarningsInnerSeverityEnum? severity,
  ) => call(severity: severity);

  @override
  SystemInfoWarningsInner message(String message) => call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoWarningsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoWarningsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoWarningsInner call({
    Object? code = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoWarningsInner(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as SystemInfoWarningsInnerSeverityEnum?,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
    );
  }
}

extension $SystemInfoWarningsInnerCopyWith on SystemInfoWarningsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoWarningsInner.copyWith(...)` or `instanceOfSystemInfoWarningsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoWarningsInnerCWProxy get copyWith =>
      _$SystemInfoWarningsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoWarningsInner _$SystemInfoWarningsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemInfoWarningsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['code', 'message']);
  final val = SystemInfoWarningsInner(
    code: $checkedConvert('code', (v) => v as String),
    severity: $checkedConvert(
      'severity',
      (v) =>
          $enumDecodeNullable(_$SystemInfoWarningsInnerSeverityEnumEnumMap, v),
    ),
    message: $checkedConvert('message', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$SystemInfoWarningsInnerToJson(
  SystemInfoWarningsInner instance,
) => <String, dynamic>{
  'code': instance.code,
  'severity': ?_$SystemInfoWarningsInnerSeverityEnumEnumMap[instance.severity],
  'message': instance.message,
};

const _$SystemInfoWarningsInnerSeverityEnumEnumMap = {
  SystemInfoWarningsInnerSeverityEnum.info: 'info',
  SystemInfoWarningsInnerSeverityEnum.warning: 'warning',
  SystemInfoWarningsInnerSeverityEnum.critical: 'critical',
};
