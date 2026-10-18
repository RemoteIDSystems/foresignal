// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_status_denied.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkStatusDeniedCWProxy {
  SinkStatusDenied total(int total);

  SinkStatusDenied byReason(Map<String, int>? byReason);

  SinkStatusDenied bySource(Map<String, int>? bySource);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkStatusDenied(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkStatusDenied(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkStatusDenied call({
    int total,
    Map<String, int>? byReason,
    Map<String, int>? bySource,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkStatusDenied.copyWith(...)` or call `instanceOfSinkStatusDenied.copyWith.fieldName(value)` for a single field.
class _$SinkStatusDeniedCWProxyImpl implements _$SinkStatusDeniedCWProxy {
  const _$SinkStatusDeniedCWProxyImpl(this._value);

  final SinkStatusDenied _value;

  @override
  SinkStatusDenied total(int total) => call(total: total);

  @override
  SinkStatusDenied byReason(Map<String, int>? byReason) =>
      call(byReason: byReason);

  @override
  SinkStatusDenied bySource(Map<String, int>? bySource) =>
      call(bySource: bySource);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkStatusDenied(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkStatusDenied(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkStatusDenied call({
    Object? total = const $CopyWithPlaceholder(),
    Object? byReason = const $CopyWithPlaceholder(),
    Object? bySource = const $CopyWithPlaceholder(),
  }) {
    return SinkStatusDenied(
      total: total == const $CopyWithPlaceholder() || total == null
          ? _value.total
          // ignore: cast_nullable_to_non_nullable
          : total as int,
      byReason: byReason == const $CopyWithPlaceholder()
          ? _value.byReason
          // ignore: cast_nullable_to_non_nullable
          : byReason as Map<String, int>?,
      bySource: bySource == const $CopyWithPlaceholder()
          ? _value.bySource
          // ignore: cast_nullable_to_non_nullable
          : bySource as Map<String, int>?,
    );
  }
}

extension $SinkStatusDeniedCopyWith on SinkStatusDenied {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkStatusDenied.copyWith(...)` or `instanceOfSinkStatusDenied.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkStatusDeniedCWProxy get copyWith => _$SinkStatusDeniedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkStatusDenied _$SinkStatusDeniedFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SinkStatusDenied', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['total']);
      final val = SinkStatusDenied(
        total: $checkedConvert('total', (v) => (v as num).toInt()),
        byReason: $checkedConvert(
          'byReason',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        bySource: $checkedConvert(
          'bySource',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SinkStatusDeniedToJson(SinkStatusDenied instance) =>
    <String, dynamic>{
      'total': instance.total,
      'byReason': ?instance.byReason,
      'bySource': ?instance.bySource,
    };
