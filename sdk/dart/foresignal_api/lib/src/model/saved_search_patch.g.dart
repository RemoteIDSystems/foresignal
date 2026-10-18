// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchPatchCWProxy {
  SavedSearchPatch name(String? name);

  SavedSearchPatch query(SavedSearchQuery? query);

  SavedSearchPatch visibility(Visibility? visibility);

  SavedSearchPatch tlp(Tlp? tlp);

  SavedSearchPatch notify(bool? notify);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearchPatch call({
    String? name,
    SavedSearchQuery? query,
    Visibility? visibility,
    Tlp? tlp,
    bool? notify,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearchPatch.copyWith(...)` or call `instanceOfSavedSearchPatch.copyWith.fieldName(value)` for a single field.
class _$SavedSearchPatchCWProxyImpl implements _$SavedSearchPatchCWProxy {
  const _$SavedSearchPatchCWProxyImpl(this._value);

  final SavedSearchPatch _value;

  @override
  SavedSearchPatch name(String? name) => call(name: name);

  @override
  SavedSearchPatch query(SavedSearchQuery? query) => call(query: query);

  @override
  SavedSearchPatch visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  SavedSearchPatch tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  SavedSearchPatch notify(bool? notify) => call(notify: notify);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearchPatch call({
    Object? name = const $CopyWithPlaceholder(),
    Object? query = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? notify = const $CopyWithPlaceholder(),
  }) {
    return SavedSearchPatch(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      query: query == const $CopyWithPlaceholder()
          ? _value.query
          // ignore: cast_nullable_to_non_nullable
          : query as SavedSearchQuery?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      notify: notify == const $CopyWithPlaceholder()
          ? _value.notify
          // ignore: cast_nullable_to_non_nullable
          : notify as bool?,
    );
  }
}

extension $SavedSearchPatchCopyWith on SavedSearchPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearchPatch.copyWith(...)` or `instanceOfSavedSearchPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchPatchCWProxy get copyWith => _$SavedSearchPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearchPatch _$SavedSearchPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SavedSearchPatch', json, ($checkedConvert) {
      final val = SavedSearchPatch(
        name: $checkedConvert('name', (v) => v as String?),
        query: $checkedConvert(
          'query',
          (v) => v == null
              ? null
              : SavedSearchQuery.fromJson(v as Map<String, dynamic>),
        ),
        visibility: $checkedConvert(
          'visibility',
          (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
        ),
        tlp: $checkedConvert(
          'tlp',
          (v) => $enumDecodeNullable(_$TlpEnumMap, v),
        ),
        notify: $checkedConvert('notify', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$SavedSearchPatchToJson(SavedSearchPatch instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'query': ?instance.query?.toJson(),
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'tlp': ?_$TlpEnumMap[instance.tlp],
      'notify': ?instance.notify,
    };

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
