// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_source_info.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpSourceInfoCWProxy {
  EdpSourceInfo id(String id);

  EdpSourceInfo name(String name);

  EdpSourceInfo description(String? description);

  EdpSourceInfo version(String? version);

  EdpSourceInfo publisher(EdpSourceInfoPublisher publisher);

  EdpSourceInfo homepage(String? homepage);

  EdpSourceInfo licence(EdpSourceInfoLicence? licence);

  EdpSourceInfo terms(EdpSourceInfoTerms? terms);

  EdpSourceInfo access(EdpSourceInfoAccess? access);

  EdpSourceInfo originator(EdpSourceInfoOriginator originator);

  EdpSourceInfo private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpSourceInfo call({
    String id,
    String name,
    String? description,
    String? version,
    EdpSourceInfoPublisher publisher,
    String? homepage,
    EdpSourceInfoLicence? licence,
    EdpSourceInfoTerms? terms,
    EdpSourceInfoAccess? access,
    EdpSourceInfoOriginator originator,
    bool? private,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpSourceInfo.copyWith(...)` or call `instanceOfEdpSourceInfo.copyWith.fieldName(value)` for a single field.
class _$EdpSourceInfoCWProxyImpl implements _$EdpSourceInfoCWProxy {
  const _$EdpSourceInfoCWProxyImpl(this._value);

  final EdpSourceInfo _value;

  @override
  EdpSourceInfo id(String id) => call(id: id);

  @override
  EdpSourceInfo name(String name) => call(name: name);

  @override
  EdpSourceInfo description(String? description) =>
      call(description: description);

  @override
  EdpSourceInfo version(String? version) => call(version: version);

  @override
  EdpSourceInfo publisher(EdpSourceInfoPublisher publisher) =>
      call(publisher: publisher);

  @override
  EdpSourceInfo homepage(String? homepage) => call(homepage: homepage);

  @override
  EdpSourceInfo licence(EdpSourceInfoLicence? licence) =>
      call(licence: licence);

  @override
  EdpSourceInfo terms(EdpSourceInfoTerms? terms) => call(terms: terms);

  @override
  EdpSourceInfo access(EdpSourceInfoAccess? access) => call(access: access);

  @override
  EdpSourceInfo originator(EdpSourceInfoOriginator originator) =>
      call(originator: originator);

  @override
  EdpSourceInfo private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpSourceInfo call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? publisher = const $CopyWithPlaceholder(),
    Object? homepage = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? terms = const $CopyWithPlaceholder(),
    Object? access = const $CopyWithPlaceholder(),
    Object? originator = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return EdpSourceInfo(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String?,
      publisher: publisher == const $CopyWithPlaceholder() || publisher == null
          ? _value.publisher
          // ignore: cast_nullable_to_non_nullable
          : publisher as EdpSourceInfoPublisher,
      homepage: homepage == const $CopyWithPlaceholder()
          ? _value.homepage
          // ignore: cast_nullable_to_non_nullable
          : homepage as String?,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as EdpSourceInfoLicence?,
      terms: terms == const $CopyWithPlaceholder()
          ? _value.terms
          // ignore: cast_nullable_to_non_nullable
          : terms as EdpSourceInfoTerms?,
      access: access == const $CopyWithPlaceholder()
          ? _value.access
          // ignore: cast_nullable_to_non_nullable
          : access as EdpSourceInfoAccess?,
      originator:
          originator == const $CopyWithPlaceholder() || originator == null
          ? _value.originator
          // ignore: cast_nullable_to_non_nullable
          : originator as EdpSourceInfoOriginator,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
    );
  }
}

extension $EdpSourceInfoCopyWith on EdpSourceInfo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpSourceInfo.copyWith(...)` or `instanceOfEdpSourceInfo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpSourceInfoCWProxy get copyWith => _$EdpSourceInfoCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpSourceInfo _$EdpSourceInfoFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpSourceInfo', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'name', 'publisher', 'originator'],
      );
      final val = EdpSourceInfo(
        id: $checkedConvert('id', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        version: $checkedConvert('version', (v) => v as String?),
        publisher: $checkedConvert(
          'publisher',
          (v) => EdpSourceInfoPublisher.fromJson(v as Map<String, dynamic>),
        ),
        homepage: $checkedConvert('homepage', (v) => v as String?),
        licence: $checkedConvert(
          'licence',
          (v) => v == null
              ? null
              : EdpSourceInfoLicence.fromJson(v as Map<String, dynamic>),
        ),
        terms: $checkedConvert(
          'terms',
          (v) => v == null
              ? null
              : EdpSourceInfoTerms.fromJson(v as Map<String, dynamic>),
        ),
        access: $checkedConvert(
          'access',
          (v) => v == null
              ? null
              : EdpSourceInfoAccess.fromJson(v as Map<String, dynamic>),
        ),
        originator: $checkedConvert(
          'originator',
          (v) => EdpSourceInfoOriginator.fromJson(v as Map<String, dynamic>),
        ),
        private: $checkedConvert('private', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$EdpSourceInfoToJson(EdpSourceInfo instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'description': ?instance.description,
      'version': ?instance.version,
      'publisher': instance.publisher.toJson(),
      'homepage': ?instance.homepage,
      'licence': ?instance.licence?.toJson(),
      'terms': ?instance.terms?.toJson(),
      'access': ?instance.access?.toJson(),
      'originator': instance.originator.toJson(),
      'private': ?instance.private,
    };
