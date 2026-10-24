// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_cue_external_results_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCueExternalResultsInnerCWProxy {
  SocialCueExternalResultsInner sourceId(String sourceId);

  SocialCueExternalResultsInner queries(int queries);

  SocialCueExternalResultsInner items(int items);

  SocialCueExternalResultsInner error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueExternalResultsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueExternalResultsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCueExternalResultsInner call({
    String sourceId,
    int queries,
    int items,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCueExternalResultsInner.copyWith(...)` or call `instanceOfSocialCueExternalResultsInner.copyWith.fieldName(value)` for a single field.
class _$SocialCueExternalResultsInnerCWProxyImpl
    implements _$SocialCueExternalResultsInnerCWProxy {
  const _$SocialCueExternalResultsInnerCWProxyImpl(this._value);

  final SocialCueExternalResultsInner _value;

  @override
  SocialCueExternalResultsInner sourceId(String sourceId) =>
      call(sourceId: sourceId);

  @override
  SocialCueExternalResultsInner queries(int queries) => call(queries: queries);

  @override
  SocialCueExternalResultsInner items(int items) => call(items: items);

  @override
  SocialCueExternalResultsInner error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueExternalResultsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueExternalResultsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCueExternalResultsInner call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? queries = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return SocialCueExternalResultsInner(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      queries: queries == const $CopyWithPlaceholder() || queries == null
          ? _value.queries
          // ignore: cast_nullable_to_non_nullable
          : queries as int,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as int,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $SocialCueExternalResultsInnerCopyWith
    on SocialCueExternalResultsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCueExternalResultsInner.copyWith(...)` or `instanceOfSocialCueExternalResultsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCueExternalResultsInnerCWProxy get copyWith =>
      _$SocialCueExternalResultsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCueExternalResultsInner _$SocialCueExternalResultsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialCueExternalResultsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['sourceId', 'queries', 'items']);
  final val = SocialCueExternalResultsInner(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    queries: $checkedConvert('queries', (v) => (v as num).toInt()),
    items: $checkedConvert('items', (v) => (v as num).toInt()),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SocialCueExternalResultsInnerToJson(
  SocialCueExternalResultsInner instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'queries': instance.queries,
  'items': instance.items,
  'error': ?instance.error,
};
