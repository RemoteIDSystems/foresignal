// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_external_stream_selector.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleExternalStreamSelectorCWProxy {
  ChronicleExternalStreamSelector type(
    ChronicleExternalStreamSelectorTypeEnum type,
  );

  ChronicleExternalStreamSelector sourceId(String sourceId);

  ChronicleExternalStreamSelector streamId(String streamId);

  ChronicleExternalStreamSelector filter(List<Map<String, Object>>? filter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleExternalStreamSelector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleExternalStreamSelector(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleExternalStreamSelector call({
    ChronicleExternalStreamSelectorTypeEnum type,
    String sourceId,
    String streamId,
    List<Map<String, Object>>? filter,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleExternalStreamSelector.copyWith(...)` or call `instanceOfChronicleExternalStreamSelector.copyWith.fieldName(value)` for a single field.
class _$ChronicleExternalStreamSelectorCWProxyImpl
    implements _$ChronicleExternalStreamSelectorCWProxy {
  const _$ChronicleExternalStreamSelectorCWProxyImpl(this._value);

  final ChronicleExternalStreamSelector _value;

  @override
  ChronicleExternalStreamSelector type(
    ChronicleExternalStreamSelectorTypeEnum type,
  ) => call(type: type);

  @override
  ChronicleExternalStreamSelector sourceId(String sourceId) =>
      call(sourceId: sourceId);

  @override
  ChronicleExternalStreamSelector streamId(String streamId) =>
      call(streamId: streamId);

  @override
  ChronicleExternalStreamSelector filter(List<Map<String, Object>>? filter) =>
      call(filter: filter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleExternalStreamSelector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleExternalStreamSelector(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleExternalStreamSelector call({
    Object? type = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? streamId = const $CopyWithPlaceholder(),
    Object? filter = const $CopyWithPlaceholder(),
  }) {
    return ChronicleExternalStreamSelector(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleExternalStreamSelectorTypeEnum,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      streamId: streamId == const $CopyWithPlaceholder() || streamId == null
          ? _value.streamId
          // ignore: cast_nullable_to_non_nullable
          : streamId as String,
      filter: filter == const $CopyWithPlaceholder()
          ? _value.filter
          // ignore: cast_nullable_to_non_nullable
          : filter as List<Map<String, Object>>?,
    );
  }
}

extension $ChronicleExternalStreamSelectorCopyWith
    on ChronicleExternalStreamSelector {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleExternalStreamSelector.copyWith(...)` or `instanceOfChronicleExternalStreamSelector.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleExternalStreamSelectorCWProxy get copyWith =>
      _$ChronicleExternalStreamSelectorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleExternalStreamSelector _$ChronicleExternalStreamSelectorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleExternalStreamSelector', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'sourceId', 'streamId']);
  final val = ChronicleExternalStreamSelector(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleExternalStreamSelectorTypeEnumEnumMap, v),
    ),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    streamId: $checkedConvert('streamId', (v) => v as String),
    filter: $checkedConvert(
      'filter',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => (e as Map<String, dynamic>).map(
              (k, e) => MapEntry(k, e as Object),
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleExternalStreamSelectorToJson(
  ChronicleExternalStreamSelector instance,
) => <String, dynamic>{
  'type': _$ChronicleExternalStreamSelectorTypeEnumEnumMap[instance.type]!,
  'sourceId': instance.sourceId,
  'streamId': instance.streamId,
  'filter': ?instance.filter,
};

const _$ChronicleExternalStreamSelectorTypeEnumEnumMap = {
  ChronicleExternalStreamSelectorTypeEnum.externalStream: 'external-stream',
};
