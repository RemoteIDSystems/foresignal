// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchInputCWProxy {
  SavedSearchInput name(String name);

  SavedSearchInput query(SavedSearchQuery query);

  SavedSearchInput visibility(Visibility? visibility);

  SavedSearchInput tlp(Tlp? tlp);

  SavedSearchInput notify(bool? notify);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearchInput call({
    String name,
    SavedSearchQuery query,
    Visibility? visibility,
    Tlp? tlp,
    bool? notify,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearchInput.copyWith(...)` or call `instanceOfSavedSearchInput.copyWith.fieldName(value)` for a single field.
class _$SavedSearchInputCWProxyImpl implements _$SavedSearchInputCWProxy {
  const _$SavedSearchInputCWProxyImpl(this._value);

  final SavedSearchInput _value;

  @override
  SavedSearchInput name(String name) => call(name: name);

  @override
  SavedSearchInput query(SavedSearchQuery query) => call(query: query);

  @override
  SavedSearchInput visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  SavedSearchInput tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  SavedSearchInput notify(bool? notify) => call(notify: notify);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearchInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? query = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? notify = const $CopyWithPlaceholder(),
  }) {
    return SavedSearchInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      query: query == const $CopyWithPlaceholder() || query == null
          ? _value.query
          // ignore: cast_nullable_to_non_nullable
          : query as SavedSearchQuery,
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

extension $SavedSearchInputCopyWith on SavedSearchInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearchInput.copyWith(...)` or `instanceOfSavedSearchInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchInputCWProxy get copyWith => _$SavedSearchInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearchInput _$SavedSearchInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SavedSearchInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'query']);
      final val = SavedSearchInput(
        name: $checkedConvert('name', (v) => v as String),
        query: $checkedConvert(
          'query',
          (v) => SavedSearchQuery.fromJson(v as Map<String, dynamic>),
        ),
        visibility: $checkedConvert(
          'visibility',
          (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
        ),
        tlp: $checkedConvert(
          'tlp',
          (v) => $enumDecodeNullable(_$TlpEnumMap, v),
        ),
        notify: $checkedConvert('notify', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$SavedSearchInputToJson(SavedSearchInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'query': instance.query.toJson(),
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
