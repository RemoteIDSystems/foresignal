// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hello_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HelloEventCWProxy {
  HelloEvent streamId(String streamId);

  HelloEvent topics(List<String> topics);

  HelloEvent watermarkSeq(int watermarkSeq);

  HelloEvent filterVersion(int filterVersion);

  HelloEvent streamKey(String? streamKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HelloEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HelloEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  HelloEvent call({
    String streamId,
    List<String> topics,
    int watermarkSeq,
    int filterVersion,
    String? streamKey,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHelloEvent.copyWith(...)` or call `instanceOfHelloEvent.copyWith.fieldName(value)` for a single field.
class _$HelloEventCWProxyImpl implements _$HelloEventCWProxy {
  const _$HelloEventCWProxyImpl(this._value);

  final HelloEvent _value;

  @override
  HelloEvent streamId(String streamId) => call(streamId: streamId);

  @override
  HelloEvent topics(List<String> topics) => call(topics: topics);

  @override
  HelloEvent watermarkSeq(int watermarkSeq) => call(watermarkSeq: watermarkSeq);

  @override
  HelloEvent filterVersion(int filterVersion) =>
      call(filterVersion: filterVersion);

  @override
  HelloEvent streamKey(String? streamKey) => call(streamKey: streamKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HelloEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HelloEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HelloEvent call({
    Object? streamId = const $CopyWithPlaceholder(),
    Object? topics = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? filterVersion = const $CopyWithPlaceholder(),
    Object? streamKey = const $CopyWithPlaceholder(),
  }) {
    return HelloEvent(
      streamId: streamId == const $CopyWithPlaceholder() || streamId == null
          ? _value.streamId
          // ignore: cast_nullable_to_non_nullable
          : streamId as String,
      topics: topics == const $CopyWithPlaceholder() || topics == null
          ? _value.topics
          // ignore: cast_nullable_to_non_nullable
          : topics as List<String>,
      watermarkSeq:
          watermarkSeq == const $CopyWithPlaceholder() || watermarkSeq == null
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int,
      filterVersion:
          filterVersion == const $CopyWithPlaceholder() || filterVersion == null
          ? _value.filterVersion
          // ignore: cast_nullable_to_non_nullable
          : filterVersion as int,
      streamKey: streamKey == const $CopyWithPlaceholder()
          ? _value.streamKey
          // ignore: cast_nullable_to_non_nullable
          : streamKey as String?,
    );
  }
}

extension $HelloEventCopyWith on HelloEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHelloEvent.copyWith(...)` or `instanceOfHelloEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HelloEventCWProxy get copyWith => _$HelloEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HelloEvent _$HelloEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HelloEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['streamId', 'topics', 'watermarkSeq', 'filterVersion'],
  );
  final val = HelloEvent(
    streamId: $checkedConvert('streamId', (v) => v as String),
    topics: $checkedConvert(
      'topics',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num).toInt()),
    filterVersion: $checkedConvert('filterVersion', (v) => (v as num).toInt()),
    streamKey: $checkedConvert('streamKey', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$HelloEventToJson(HelloEvent instance) =>
    <String, dynamic>{
      'streamId': instance.streamId,
      'topics': instance.topics,
      'watermarkSeq': instance.watermarkSeq,
      'filterVersion': instance.filterVersion,
      'streamKey': ?instance.streamKey,
    };
