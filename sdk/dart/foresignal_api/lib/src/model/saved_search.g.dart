// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchCWProxy {
  SavedSearch id(String id);

  SavedSearch name(String name);

  SavedSearch owner(PrincipalSummary owner);

  SavedSearch query(SavedSearchQuery query);

  SavedSearch visibility(Visibility visibility);

  SavedSearch tlp(Tlp tlp);

  SavedSearch notify(bool notify);

  SavedSearch lastRunAt(DateTime? lastRunAt);

  SavedSearch lastNotifiedAt(DateTime? lastNotifiedAt);

  SavedSearch lastMatchCount(int? lastMatchCount);

  SavedSearch createdAt(DateTime createdAt);

  SavedSearch updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearch(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearch call({
    String id,
    String name,
    PrincipalSummary owner,
    SavedSearchQuery query,
    Visibility visibility,
    Tlp tlp,
    bool notify,
    DateTime? lastRunAt,
    DateTime? lastNotifiedAt,
    int? lastMatchCount,
    DateTime createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearch.copyWith(...)` or call `instanceOfSavedSearch.copyWith.fieldName(value)` for a single field.
class _$SavedSearchCWProxyImpl implements _$SavedSearchCWProxy {
  const _$SavedSearchCWProxyImpl(this._value);

  final SavedSearch _value;

  @override
  SavedSearch id(String id) => call(id: id);

  @override
  SavedSearch name(String name) => call(name: name);

  @override
  SavedSearch owner(PrincipalSummary owner) => call(owner: owner);

  @override
  SavedSearch query(SavedSearchQuery query) => call(query: query);

  @override
  SavedSearch visibility(Visibility visibility) => call(visibility: visibility);

  @override
  SavedSearch tlp(Tlp tlp) => call(tlp: tlp);

  @override
  SavedSearch notify(bool notify) => call(notify: notify);

  @override
  SavedSearch lastRunAt(DateTime? lastRunAt) => call(lastRunAt: lastRunAt);

  @override
  SavedSearch lastNotifiedAt(DateTime? lastNotifiedAt) =>
      call(lastNotifiedAt: lastNotifiedAt);

  @override
  SavedSearch lastMatchCount(int? lastMatchCount) =>
      call(lastMatchCount: lastMatchCount);

  @override
  SavedSearch createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  SavedSearch updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearch call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? owner = const $CopyWithPlaceholder(),
    Object? query = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? notify = const $CopyWithPlaceholder(),
    Object? lastRunAt = const $CopyWithPlaceholder(),
    Object? lastNotifiedAt = const $CopyWithPlaceholder(),
    Object? lastMatchCount = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return SavedSearch(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      owner: owner == const $CopyWithPlaceholder() || owner == null
          ? _value.owner
          // ignore: cast_nullable_to_non_nullable
          : owner as PrincipalSummary,
      query: query == const $CopyWithPlaceholder() || query == null
          ? _value.query
          // ignore: cast_nullable_to_non_nullable
          : query as SavedSearchQuery,
      visibility:
          visibility == const $CopyWithPlaceholder() || visibility == null
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      notify: notify == const $CopyWithPlaceholder() || notify == null
          ? _value.notify
          // ignore: cast_nullable_to_non_nullable
          : notify as bool,
      lastRunAt: lastRunAt == const $CopyWithPlaceholder()
          ? _value.lastRunAt
          // ignore: cast_nullable_to_non_nullable
          : lastRunAt as DateTime?,
      lastNotifiedAt: lastNotifiedAt == const $CopyWithPlaceholder()
          ? _value.lastNotifiedAt
          // ignore: cast_nullable_to_non_nullable
          : lastNotifiedAt as DateTime?,
      lastMatchCount: lastMatchCount == const $CopyWithPlaceholder()
          ? _value.lastMatchCount
          // ignore: cast_nullable_to_non_nullable
          : lastMatchCount as int?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $SavedSearchCopyWith on SavedSearch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearch.copyWith(...)` or `instanceOfSavedSearch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchCWProxy get copyWith => _$SavedSearchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearch _$SavedSearchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SavedSearch', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'name',
          'owner',
          'query',
          'visibility',
          'tlp',
          'notify',
          'createdAt',
        ],
      );
      final val = SavedSearch(
        id: $checkedConvert('id', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        owner: $checkedConvert(
          'owner',
          (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        query: $checkedConvert(
          'query',
          (v) => SavedSearchQuery.fromJson(v as Map<String, dynamic>),
        ),
        visibility: $checkedConvert(
          'visibility',
          (v) => $enumDecode(_$VisibilityEnumMap, v),
        ),
        tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
        notify: $checkedConvert('notify', (v) => v as bool),
        lastRunAt: $checkedConvert(
          'lastRunAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastNotifiedAt: $checkedConvert(
          'lastNotifiedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastMatchCount: $checkedConvert(
          'lastMatchCount',
          (v) => (v as num?)?.toInt(),
        ),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SavedSearchToJson(SavedSearch instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'owner': instance.owner.toJson(),
      'query': instance.query.toJson(),
      'visibility': _$VisibilityEnumMap[instance.visibility]!,
      'tlp': _$TlpEnumMap[instance.tlp]!,
      'notify': instance.notify,
      'lastRunAt': ?instance.lastRunAt?.toIso8601String(),
      'lastNotifiedAt': ?instance.lastNotifiedAt?.toIso8601String(),
      'lastMatchCount': ?instance.lastMatchCount,
      'createdAt': instance.createdAt.toIso8601String(),
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
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
