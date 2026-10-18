// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_draft_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetDraftRequestCWProxy {
  HypothesisSetDraftRequest text(String text);

  HypothesisSetDraftRequest situationId(String? situationId);

  HypothesisSetDraftRequest regionCodes(List<String>? regionCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetDraftRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetDraftRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetDraftRequest call({
    String text,
    String? situationId,
    List<String>? regionCodes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetDraftRequest.copyWith(...)` or call `instanceOfHypothesisSetDraftRequest.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetDraftRequestCWProxyImpl
    implements _$HypothesisSetDraftRequestCWProxy {
  const _$HypothesisSetDraftRequestCWProxyImpl(this._value);

  final HypothesisSetDraftRequest _value;

  @override
  HypothesisSetDraftRequest text(String text) => call(text: text);

  @override
  HypothesisSetDraftRequest situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  HypothesisSetDraftRequest regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetDraftRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetDraftRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetDraftRequest call({
    Object? text = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetDraftRequest(
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
    );
  }
}

extension $HypothesisSetDraftRequestCopyWith on HypothesisSetDraftRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetDraftRequest.copyWith(...)` or `instanceOfHypothesisSetDraftRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetDraftRequestCWProxy get copyWith =>
      _$HypothesisSetDraftRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetDraftRequest _$HypothesisSetDraftRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisSetDraftRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['text']);
  final val = HypothesisSetDraftRequest(
    text: $checkedConvert('text', (v) => v as String),
    situationId: $checkedConvert('situationId', (v) => v as String?),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$HypothesisSetDraftRequestToJson(
  HypothesisSetDraftRequest instance,
) => <String, dynamic>{
  'text': instance.text,
  'situationId': ?instance.situationId,
  'regionCodes': ?instance.regionCodes,
};
