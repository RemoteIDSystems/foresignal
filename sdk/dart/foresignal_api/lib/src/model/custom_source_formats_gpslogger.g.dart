// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'custom_source_formats_gpslogger.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CustomSourceFormatsGpsloggerCWProxy {
  CustomSourceFormatsGpslogger maxAccuracyMeters(num? maxAccuracyMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsGpslogger(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsGpslogger(...).copyWith(id: 12, name: "My name")
  /// ```
  CustomSourceFormatsGpslogger call({num? maxAccuracyMeters});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCustomSourceFormatsGpslogger.copyWith(...)` or call `instanceOfCustomSourceFormatsGpslogger.copyWith.fieldName(value)` for a single field.
class _$CustomSourceFormatsGpsloggerCWProxyImpl
    implements _$CustomSourceFormatsGpsloggerCWProxy {
  const _$CustomSourceFormatsGpsloggerCWProxyImpl(this._value);

  final CustomSourceFormatsGpslogger _value;

  @override
  CustomSourceFormatsGpslogger maxAccuracyMeters(num? maxAccuracyMeters) =>
      call(maxAccuracyMeters: maxAccuracyMeters);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormatsGpslogger(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormatsGpslogger(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CustomSourceFormatsGpslogger call({
    Object? maxAccuracyMeters = const $CopyWithPlaceholder(),
  }) {
    return CustomSourceFormatsGpslogger(
      maxAccuracyMeters: maxAccuracyMeters == const $CopyWithPlaceholder()
          ? _value.maxAccuracyMeters
          // ignore: cast_nullable_to_non_nullable
          : maxAccuracyMeters as num?,
    );
  }
}

extension $CustomSourceFormatsGpsloggerCopyWith
    on CustomSourceFormatsGpslogger {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCustomSourceFormatsGpslogger.copyWith(...)` or `instanceOfCustomSourceFormatsGpslogger.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CustomSourceFormatsGpsloggerCWProxy get copyWith =>
      _$CustomSourceFormatsGpsloggerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CustomSourceFormatsGpslogger _$CustomSourceFormatsGpsloggerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CustomSourceFormatsGpslogger', json, ($checkedConvert) {
  final val = CustomSourceFormatsGpslogger(
    maxAccuracyMeters: $checkedConvert(
      'maxAccuracyMeters',
      (v) => v as num? ?? 200,
    ),
  );
  return val;
});

Map<String, dynamic> _$CustomSourceFormatsGpsloggerToJson(
  CustomSourceFormatsGpslogger instance,
) => <String, dynamic>{'maxAccuracyMeters': ?instance.maxAccuracyMeters};
