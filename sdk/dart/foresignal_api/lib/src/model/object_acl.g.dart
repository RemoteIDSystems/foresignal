// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_acl.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectAclCWProxy {
  ObjectAcl ref(String ref);

  ObjectAcl type(ShareableType type);

  ObjectAcl owner(PrincipalSummary owner);

  ObjectAcl visibility(Visibility visibility);

  ObjectAcl tlp(Tlp tlp);

  ObjectAcl entries(List<ObjectAclEntry> entries);

  ObjectAcl updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAcl(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAcl(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectAcl call({
    String ref,
    ShareableType type,
    PrincipalSummary owner,
    Visibility visibility,
    Tlp tlp,
    List<ObjectAclEntry> entries,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectAcl.copyWith(...)` or call `instanceOfObjectAcl.copyWith.fieldName(value)` for a single field.
class _$ObjectAclCWProxyImpl implements _$ObjectAclCWProxy {
  const _$ObjectAclCWProxyImpl(this._value);

  final ObjectAcl _value;

  @override
  ObjectAcl ref(String ref) => call(ref: ref);

  @override
  ObjectAcl type(ShareableType type) => call(type: type);

  @override
  ObjectAcl owner(PrincipalSummary owner) => call(owner: owner);

  @override
  ObjectAcl visibility(Visibility visibility) => call(visibility: visibility);

  @override
  ObjectAcl tlp(Tlp tlp) => call(tlp: tlp);

  @override
  ObjectAcl entries(List<ObjectAclEntry> entries) => call(entries: entries);

  @override
  ObjectAcl updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAcl(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAcl(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectAcl call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? owner = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? entries = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return ObjectAcl(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ShareableType,
      owner: owner == const $CopyWithPlaceholder() || owner == null
          ? _value.owner
          // ignore: cast_nullable_to_non_nullable
          : owner as PrincipalSummary,
      visibility:
          visibility == const $CopyWithPlaceholder() || visibility == null
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      entries: entries == const $CopyWithPlaceholder() || entries == null
          ? _value.entries
          // ignore: cast_nullable_to_non_nullable
          : entries as List<ObjectAclEntry>,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $ObjectAclCopyWith on ObjectAcl {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectAcl.copyWith(...)` or `instanceOfObjectAcl.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectAclCWProxy get copyWith => _$ObjectAclCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectAcl _$ObjectAclFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ObjectAcl', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'ref',
          'type',
          'owner',
          'visibility',
          'tlp',
          'entries',
        ],
      );
      final val = ObjectAcl(
        ref: $checkedConvert('ref', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ShareableTypeEnumMap, v),
        ),
        owner: $checkedConvert(
          'owner',
          (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        visibility: $checkedConvert(
          'visibility',
          (v) => $enumDecode(_$VisibilityEnumMap, v),
        ),
        tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
        entries: $checkedConvert(
          'entries',
          (v) => (v as List<dynamic>)
              .map((e) => ObjectAclEntry.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ObjectAclToJson(ObjectAcl instance) => <String, dynamic>{
  'ref': instance.ref,
  'type': _$ShareableTypeEnumMap[instance.type]!,
  'owner': instance.owner.toJson(),
  'visibility': _$VisibilityEnumMap[instance.visibility]!,
  'tlp': _$TlpEnumMap[instance.tlp]!,
  'entries': instance.entries.map((e) => e.toJson()).toList(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$ShareableTypeEnumMap = {
  ShareableType.investigation: 'investigation',
  ShareableType.workspace: 'workspace',
  ShareableType.chronicle: 'chronicle',
  ShareableType.report: 'report',
  ShareableType.savedSearch: 'saved-search',
  ShareableType.annotation: 'annotation',
  ShareableType.attachment: 'attachment',
  ShareableType.evidenceItem: 'evidence-item',
  ShareableType.tag: 'tag',
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
