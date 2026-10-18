// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_raster_formats_webp.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoRasterFormatsWebpCWProxy {
  SystemInfoRasterFormatsWebp available(bool? available);

  SystemInfoRasterFormatsWebp reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoRasterFormatsWebp(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoRasterFormatsWebp(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoRasterFormatsWebp call({bool? available, String? reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoRasterFormatsWebp.copyWith(...)` or call `instanceOfSystemInfoRasterFormatsWebp.copyWith.fieldName(value)` for a single field.
class _$SystemInfoRasterFormatsWebpCWProxyImpl
    implements _$SystemInfoRasterFormatsWebpCWProxy {
  const _$SystemInfoRasterFormatsWebpCWProxyImpl(this._value);

  final SystemInfoRasterFormatsWebp _value;

  @override
  SystemInfoRasterFormatsWebp available(bool? available) =>
      call(available: available);

  @override
  SystemInfoRasterFormatsWebp reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoRasterFormatsWebp(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoRasterFormatsWebp(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoRasterFormatsWebp call({
    Object? available = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoRasterFormatsWebp(
      available: available == const $CopyWithPlaceholder()
          ? _value.available
          // ignore: cast_nullable_to_non_nullable
          : available as bool?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $SystemInfoRasterFormatsWebpCopyWith on SystemInfoRasterFormatsWebp {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoRasterFormatsWebp.copyWith(...)` or `instanceOfSystemInfoRasterFormatsWebp.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoRasterFormatsWebpCWProxy get copyWith =>
      _$SystemInfoRasterFormatsWebpCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoRasterFormatsWebp _$SystemInfoRasterFormatsWebpFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemInfoRasterFormatsWebp', json, ($checkedConvert) {
  final val = SystemInfoRasterFormatsWebp(
    available: $checkedConvert('available', (v) => v as bool?),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SystemInfoRasterFormatsWebpToJson(
  SystemInfoRasterFormatsWebp instance,
) => <String, dynamic>{
  'available': ?instance.available,
  'reason': ?instance.reason,
};
