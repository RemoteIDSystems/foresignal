// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search_hit.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchHitCWProxy {
  SavedSearchHit envelope(ObjectEnvelope envelope);

  SavedSearchHit score(num? score);

  SavedSearchHit matchedBy(SavedSearchHitMatchedByEnum? matchedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchHit(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchHit(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearchHit call({
    ObjectEnvelope envelope,
    num? score,
    SavedSearchHitMatchedByEnum? matchedBy,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearchHit.copyWith(...)` or call `instanceOfSavedSearchHit.copyWith.fieldName(value)` for a single field.
class _$SavedSearchHitCWProxyImpl implements _$SavedSearchHitCWProxy {
  const _$SavedSearchHitCWProxyImpl(this._value);

  final SavedSearchHit _value;

  @override
  SavedSearchHit envelope(ObjectEnvelope envelope) => call(envelope: envelope);

  @override
  SavedSearchHit score(num? score) => call(score: score);

  @override
  SavedSearchHit matchedBy(SavedSearchHitMatchedByEnum? matchedBy) =>
      call(matchedBy: matchedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchHit(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchHit(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearchHit call({
    Object? envelope = const $CopyWithPlaceholder(),
    Object? score = const $CopyWithPlaceholder(),
    Object? matchedBy = const $CopyWithPlaceholder(),
  }) {
    return SavedSearchHit(
      envelope: envelope == const $CopyWithPlaceholder() || envelope == null
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope,
      score: score == const $CopyWithPlaceholder()
          ? _value.score
          // ignore: cast_nullable_to_non_nullable
          : score as num?,
      matchedBy: matchedBy == const $CopyWithPlaceholder()
          ? _value.matchedBy
          // ignore: cast_nullable_to_non_nullable
          : matchedBy as SavedSearchHitMatchedByEnum?,
    );
  }
}

extension $SavedSearchHitCopyWith on SavedSearchHit {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearchHit.copyWith(...)` or `instanceOfSavedSearchHit.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchHitCWProxy get copyWith => _$SavedSearchHitCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearchHit _$SavedSearchHitFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SavedSearchHit', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['envelope']);
      final val = SavedSearchHit(
        envelope: $checkedConvert(
          'envelope',
          (v) => ObjectEnvelope.fromJson(v as Map<String, dynamic>),
        ),
        score: $checkedConvert('score', (v) => v as num?),
        matchedBy: $checkedConvert(
          'matchedBy',
          (v) => $enumDecodeNullable(_$SavedSearchHitMatchedByEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SavedSearchHitToJson(SavedSearchHit instance) =>
    <String, dynamic>{
      'envelope': instance.envelope.toJson(),
      'score': ?instance.score,
      'matchedBy': ?_$SavedSearchHitMatchedByEnumEnumMap[instance.matchedBy],
    };

const _$SavedSearchHitMatchedByEnumEnumMap = {
  SavedSearchHitMatchedByEnum.text: 'text',
  SavedSearchHitMatchedByEnum.semantic: 'semantic',
  SavedSearchHitMatchedByEnum.hybrid: 'hybrid',
  SavedSearchHitMatchedByEnum.filter: 'filter',
};
