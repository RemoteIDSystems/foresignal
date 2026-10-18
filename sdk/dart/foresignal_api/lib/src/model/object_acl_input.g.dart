// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_acl_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectAclInputCWProxy {
  ObjectAclInput visibility(Visibility? visibility);

  ObjectAclInput entries(List<ObjectAclEntryInput> entries);

  ObjectAclInput owner(String? owner);

  ObjectAclInput tlp(Tlp? tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAclInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAclInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectAclInput call({
    Visibility? visibility,
    List<ObjectAclEntryInput> entries,
    String? owner,
    Tlp? tlp,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectAclInput.copyWith(...)` or call `instanceOfObjectAclInput.copyWith.fieldName(value)` for a single field.
class _$ObjectAclInputCWProxyImpl implements _$ObjectAclInputCWProxy {
  const _$ObjectAclInputCWProxyImpl(this._value);

  final ObjectAclInput _value;

  @override
  ObjectAclInput visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  ObjectAclInput entries(List<ObjectAclEntryInput> entries) =>
      call(entries: entries);

  @override
  ObjectAclInput owner(String? owner) => call(owner: owner);

  @override
  ObjectAclInput tlp(Tlp? tlp) => call(tlp: tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectAclInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectAclInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectAclInput call({
    Object? visibility = const $CopyWithPlaceholder(),
    Object? entries = const $CopyWithPlaceholder(),
    Object? owner = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
  }) {
    return ObjectAclInput(
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      entries: entries == const $CopyWithPlaceholder() || entries == null
          ? _value.entries
          // ignore: cast_nullable_to_non_nullable
          : entries as List<ObjectAclEntryInput>,
      owner: owner == const $CopyWithPlaceholder()
          ? _value.owner
          // ignore: cast_nullable_to_non_nullable
          : owner as String?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
    );
  }
}

extension $ObjectAclInputCopyWith on ObjectAclInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectAclInput.copyWith(...)` or `instanceOfObjectAclInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectAclInputCWProxy get copyWith => _$ObjectAclInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectAclInput _$ObjectAclInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ObjectAclInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['entries']);
  final val = ObjectAclInput(
    visibility: $checkedConvert(
      'visibility',
      (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
    ),
    entries: $checkedConvert(
      'entries',
      (v) => (v as List<dynamic>)
          .map((e) => ObjectAclEntryInput.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    owner: $checkedConvert('owner', (v) => v as String?),
    tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
  );
  return val;
});

Map<String, dynamic> _$ObjectAclInputToJson(ObjectAclInput instance) =>
    <String, dynamic>{
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'entries': instance.entries.map((e) => e.toJson()).toList(),
      'owner': ?instance.owner,
      'tlp': ?_$TlpEnumMap[instance.tlp],
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
