// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taxii_collection.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TaxiiCollectionCWProxy {
  TaxiiCollection id(String id);

  TaxiiCollection title(String title);

  TaxiiCollection description(String? description);

  TaxiiCollection canRead(bool canRead);

  TaxiiCollection canWrite(bool canWrite);

  TaxiiCollection mediaTypes(List<String>? mediaTypes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  TaxiiCollection call({
    String id,
    String title,
    String? description,
    bool canRead,
    bool canWrite,
    List<String>? mediaTypes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTaxiiCollection.copyWith(...)` or call `instanceOfTaxiiCollection.copyWith.fieldName(value)` for a single field.
class _$TaxiiCollectionCWProxyImpl implements _$TaxiiCollectionCWProxy {
  const _$TaxiiCollectionCWProxyImpl(this._value);

  final TaxiiCollection _value;

  @override
  TaxiiCollection id(String id) => call(id: id);

  @override
  TaxiiCollection title(String title) => call(title: title);

  @override
  TaxiiCollection description(String? description) =>
      call(description: description);

  @override
  TaxiiCollection canRead(bool canRead) => call(canRead: canRead);

  @override
  TaxiiCollection canWrite(bool canWrite) => call(canWrite: canWrite);

  @override
  TaxiiCollection mediaTypes(List<String>? mediaTypes) =>
      call(mediaTypes: mediaTypes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TaxiiCollection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TaxiiCollection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TaxiiCollection call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? canRead = const $CopyWithPlaceholder(),
    Object? canWrite = const $CopyWithPlaceholder(),
    Object? mediaTypes = const $CopyWithPlaceholder(),
  }) {
    return TaxiiCollection(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      canRead: canRead == const $CopyWithPlaceholder() || canRead == null
          ? _value.canRead
          // ignore: cast_nullable_to_non_nullable
          : canRead as bool,
      canWrite: canWrite == const $CopyWithPlaceholder() || canWrite == null
          ? _value.canWrite
          // ignore: cast_nullable_to_non_nullable
          : canWrite as bool,
      mediaTypes: mediaTypes == const $CopyWithPlaceholder()
          ? _value.mediaTypes
          // ignore: cast_nullable_to_non_nullable
          : mediaTypes as List<String>?,
    );
  }
}

extension $TaxiiCollectionCopyWith on TaxiiCollection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTaxiiCollection.copyWith(...)` or `instanceOfTaxiiCollection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TaxiiCollectionCWProxy get copyWith => _$TaxiiCollectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TaxiiCollection _$TaxiiCollectionFromJson(Map<String, dynamic> json) =>
    $checkedCreate(
      'TaxiiCollection',
      json,
      ($checkedConvert) {
        $checkKeys(
          json,
          requiredKeys: const ['id', 'title', 'can_read', 'can_write'],
        );
        final val = TaxiiCollection(
          id: $checkedConvert('id', (v) => v as String),
          title: $checkedConvert('title', (v) => v as String),
          description: $checkedConvert('description', (v) => v as String?),
          canRead: $checkedConvert('can_read', (v) => v as bool),
          canWrite: $checkedConvert('can_write', (v) => v as bool),
          mediaTypes: $checkedConvert(
            'media_types',
            (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
          ),
        );
        return val;
      },
      fieldKeyMap: const {
        'canRead': 'can_read',
        'canWrite': 'can_write',
        'mediaTypes': 'media_types',
      },
    );

Map<String, dynamic> _$TaxiiCollectionToJson(TaxiiCollection instance) =>
    <String, dynamic>{
      'id': instance.id,
      'title': instance.title,
      'description': ?instance.description,
      'can_read': instance.canRead,
      'can_write': instance.canWrite,
      'media_types': ?instance.mediaTypes,
    };
