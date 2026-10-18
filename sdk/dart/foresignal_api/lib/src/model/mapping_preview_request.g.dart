// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_preview_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingPreviewRequestCWProxy {
  MappingPreviewRequest mapping(RecordMapping mapping);

  MappingPreviewRequest sample(MappingSample sample);

  MappingPreviewRequest sourceId(String? sourceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingPreviewRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingPreviewRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingPreviewRequest call({
    RecordMapping mapping,
    MappingSample sample,
    String? sourceId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingPreviewRequest.copyWith(...)` or call `instanceOfMappingPreviewRequest.copyWith.fieldName(value)` for a single field.
class _$MappingPreviewRequestCWProxyImpl
    implements _$MappingPreviewRequestCWProxy {
  const _$MappingPreviewRequestCWProxyImpl(this._value);

  final MappingPreviewRequest _value;

  @override
  MappingPreviewRequest mapping(RecordMapping mapping) =>
      call(mapping: mapping);

  @override
  MappingPreviewRequest sample(MappingSample sample) => call(sample: sample);

  @override
  MappingPreviewRequest sourceId(String? sourceId) => call(sourceId: sourceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingPreviewRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingPreviewRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingPreviewRequest call({
    Object? mapping = const $CopyWithPlaceholder(),
    Object? sample = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
  }) {
    return MappingPreviewRequest(
      mapping: mapping == const $CopyWithPlaceholder() || mapping == null
          ? _value.mapping
          // ignore: cast_nullable_to_non_nullable
          : mapping as RecordMapping,
      sample: sample == const $CopyWithPlaceholder() || sample == null
          ? _value.sample
          // ignore: cast_nullable_to_non_nullable
          : sample as MappingSample,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
    );
  }
}

extension $MappingPreviewRequestCopyWith on MappingPreviewRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingPreviewRequest.copyWith(...)` or `instanceOfMappingPreviewRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingPreviewRequestCWProxy get copyWith =>
      _$MappingPreviewRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingPreviewRequest _$MappingPreviewRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingPreviewRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['mapping', 'sample']);
  final val = MappingPreviewRequest(
    mapping: $checkedConvert(
      'mapping',
      (v) => RecordMapping.fromJson(v as Map<String, dynamic>),
    ),
    sample: $checkedConvert(
      'sample',
      (v) => MappingSample.fromJson(v as Map<String, dynamic>),
    ),
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$MappingPreviewRequestToJson(
  MappingPreviewRequest instance,
) => <String, dynamic>{
  'mapping': instance.mapping.toJson(),
  'sample': instance.sample.toJson(),
  'sourceId': ?instance.sourceId,
};
