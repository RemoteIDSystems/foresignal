// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collection_source.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollectionSourceCWProxy {
  CollectionSource sourceId(String sourceId);

  CollectionSource inCollection(bool inCollection);

  CollectionSource enabledByUsers(int? enabledByUsers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionSource(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionSource(...).copyWith(id: 12, name: "My name")
  /// ```
  CollectionSource call({
    String sourceId,
    bool inCollection,
    int? enabledByUsers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollectionSource.copyWith(...)` or call `instanceOfCollectionSource.copyWith.fieldName(value)` for a single field.
class _$CollectionSourceCWProxyImpl implements _$CollectionSourceCWProxy {
  const _$CollectionSourceCWProxyImpl(this._value);

  final CollectionSource _value;

  @override
  CollectionSource sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  CollectionSource inCollection(bool inCollection) =>
      call(inCollection: inCollection);

  @override
  CollectionSource enabledByUsers(int? enabledByUsers) =>
      call(enabledByUsers: enabledByUsers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionSource(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionSource(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollectionSource call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? inCollection = const $CopyWithPlaceholder(),
    Object? enabledByUsers = const $CopyWithPlaceholder(),
  }) {
    return CollectionSource(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      inCollection:
          inCollection == const $CopyWithPlaceholder() || inCollection == null
          ? _value.inCollection
          // ignore: cast_nullable_to_non_nullable
          : inCollection as bool,
      enabledByUsers: enabledByUsers == const $CopyWithPlaceholder()
          ? _value.enabledByUsers
          // ignore: cast_nullable_to_non_nullable
          : enabledByUsers as int?,
    );
  }
}

extension $CollectionSourceCopyWith on CollectionSource {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollectionSource.copyWith(...)` or `instanceOfCollectionSource.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollectionSourceCWProxy get copyWith => _$CollectionSourceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectionSource _$CollectionSourceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CollectionSource', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['sourceId', 'inCollection']);
      final val = CollectionSource(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        inCollection: $checkedConvert('inCollection', (v) => v as bool),
        enabledByUsers: $checkedConvert(
          'enabledByUsers',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CollectionSourceToJson(CollectionSource instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'inCollection': instance.inCollection,
      'enabledByUsers': ?instance.enabledByUsers,
    };
