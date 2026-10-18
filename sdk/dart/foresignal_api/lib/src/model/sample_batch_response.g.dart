// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_batch_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleBatchResponseCWProxy {
  SampleBatchResponse items(List<SampleResponse> items);

  SampleBatchResponse serverTime(DateTime serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleBatchResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleBatchResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleBatchResponse call({List<SampleResponse> items, DateTime serverTime});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleBatchResponse.copyWith(...)` or call `instanceOfSampleBatchResponse.copyWith.fieldName(value)` for a single field.
class _$SampleBatchResponseCWProxyImpl implements _$SampleBatchResponseCWProxy {
  const _$SampleBatchResponseCWProxyImpl(this._value);

  final SampleBatchResponse _value;

  @override
  SampleBatchResponse items(List<SampleResponse> items) => call(items: items);

  @override
  SampleBatchResponse serverTime(DateTime serverTime) =>
      call(serverTime: serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleBatchResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleBatchResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleBatchResponse call({
    Object? items = const $CopyWithPlaceholder(),
    Object? serverTime = const $CopyWithPlaceholder(),
  }) {
    return SampleBatchResponse(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SampleResponse>,
      serverTime:
          serverTime == const $CopyWithPlaceholder() || serverTime == null
          ? _value.serverTime
          // ignore: cast_nullable_to_non_nullable
          : serverTime as DateTime,
    );
  }
}

extension $SampleBatchResponseCopyWith on SampleBatchResponse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleBatchResponse.copyWith(...)` or `instanceOfSampleBatchResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleBatchResponseCWProxy get copyWith =>
      _$SampleBatchResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleBatchResponse _$SampleBatchResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SampleBatchResponse', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'serverTime']);
      final val = SampleBatchResponse(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SampleResponse.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        serverTime: $checkedConvert(
          'serverTime',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SampleBatchResponseToJson(
  SampleBatchResponse instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'serverTime': instance.serverTime.toIso8601String(),
};
