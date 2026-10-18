// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search_matches_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchMatchesEventCWProxy {
  SavedSearchMatchesEvent savedSearchId(String savedSearchId);

  SavedSearchMatchesEvent name(String name);

  SavedSearchMatchesEvent newMatches(int newMatches);

  SavedSearchMatchesEvent refs(List<String>? refs);

  SavedSearchMatchesEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchMatchesEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchMatchesEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearchMatchesEvent call({
    String savedSearchId,
    String name,
    int newMatches,
    List<String>? refs,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearchMatchesEvent.copyWith(...)` or call `instanceOfSavedSearchMatchesEvent.copyWith.fieldName(value)` for a single field.
class _$SavedSearchMatchesEventCWProxyImpl
    implements _$SavedSearchMatchesEventCWProxy {
  const _$SavedSearchMatchesEventCWProxyImpl(this._value);

  final SavedSearchMatchesEvent _value;

  @override
  SavedSearchMatchesEvent savedSearchId(String savedSearchId) =>
      call(savedSearchId: savedSearchId);

  @override
  SavedSearchMatchesEvent name(String name) => call(name: name);

  @override
  SavedSearchMatchesEvent newMatches(int newMatches) =>
      call(newMatches: newMatches);

  @override
  SavedSearchMatchesEvent refs(List<String>? refs) => call(refs: refs);

  @override
  SavedSearchMatchesEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchMatchesEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchMatchesEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearchMatchesEvent call({
    Object? savedSearchId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? newMatches = const $CopyWithPlaceholder(),
    Object? refs = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return SavedSearchMatchesEvent(
      savedSearchId:
          savedSearchId == const $CopyWithPlaceholder() || savedSearchId == null
          ? _value.savedSearchId
          // ignore: cast_nullable_to_non_nullable
          : savedSearchId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      newMatches:
          newMatches == const $CopyWithPlaceholder() || newMatches == null
          ? _value.newMatches
          // ignore: cast_nullable_to_non_nullable
          : newMatches as int,
      refs: refs == const $CopyWithPlaceholder()
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $SavedSearchMatchesEventCopyWith on SavedSearchMatchesEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearchMatchesEvent.copyWith(...)` or `instanceOfSavedSearchMatchesEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchMatchesEventCWProxy get copyWith =>
      _$SavedSearchMatchesEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearchMatchesEvent _$SavedSearchMatchesEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SavedSearchMatchesEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['savedSearchId', 'name', 'newMatches', 'at'],
  );
  final val = SavedSearchMatchesEvent(
    savedSearchId: $checkedConvert('savedSearchId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    newMatches: $checkedConvert('newMatches', (v) => (v as num).toInt()),
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$SavedSearchMatchesEventToJson(
  SavedSearchMatchesEvent instance,
) => <String, dynamic>{
  'savedSearchId': instance.savedSearchId,
  'name': instance.name,
  'newMatches': instance.newMatches,
  'refs': ?instance.refs,
  'at': instance.at.toIso8601String(),
};
