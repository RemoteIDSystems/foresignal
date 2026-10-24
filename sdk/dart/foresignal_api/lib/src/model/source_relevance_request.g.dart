// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_relevance_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceRelevanceRequestCWProxy {
  SourceRelevanceRequest sourceIds(Set<String> sourceIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevanceRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevanceRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceRelevanceRequest call({Set<String> sourceIds});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceRelevanceRequest.copyWith(...)` or call `instanceOfSourceRelevanceRequest.copyWith.fieldName(value)` for a single field.
class _$SourceRelevanceRequestCWProxyImpl
    implements _$SourceRelevanceRequestCWProxy {
  const _$SourceRelevanceRequestCWProxyImpl(this._value);

  final SourceRelevanceRequest _value;

  @override
  SourceRelevanceRequest sourceIds(Set<String> sourceIds) =>
      call(sourceIds: sourceIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceRelevanceRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceRelevanceRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceRelevanceRequest call({
    Object? sourceIds = const $CopyWithPlaceholder(),
  }) {
    return SourceRelevanceRequest(
      sourceIds: sourceIds == const $CopyWithPlaceholder() || sourceIds == null
          ? _value.sourceIds
          // ignore: cast_nullable_to_non_nullable
          : sourceIds as Set<String>,
    );
  }
}

extension $SourceRelevanceRequestCopyWith on SourceRelevanceRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceRelevanceRequest.copyWith(...)` or `instanceOfSourceRelevanceRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceRelevanceRequestCWProxy get copyWith =>
      _$SourceRelevanceRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceRelevanceRequest _$SourceRelevanceRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceRelevanceRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['sourceIds']);
  final val = SourceRelevanceRequest(
    sourceIds: $checkedConvert(
      'sourceIds',
      (v) => (v as List<dynamic>).map((e) => e as String).toSet(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SourceRelevanceRequestToJson(
  SourceRelevanceRequest instance,
) => <String, dynamic>{'sourceIds': instance.sourceIds.toList()};
