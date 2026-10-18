// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'context_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContextLinkCWProxy {
  ContextLink version(ContextLinkVersionEnum version);

  ContextLink workspaceId(String? workspaceId);

  ContextLink payload(String payload);

  ContextLink fragment(String fragment);

  ContextLink bytes(int bytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextLink(...).copyWith(id: 12, name: "My name")
  /// ```
  ContextLink call({
    ContextLinkVersionEnum version,
    String? workspaceId,
    String payload,
    String fragment,
    int bytes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContextLink.copyWith(...)` or call `instanceOfContextLink.copyWith.fieldName(value)` for a single field.
class _$ContextLinkCWProxyImpl implements _$ContextLinkCWProxy {
  const _$ContextLinkCWProxyImpl(this._value);

  final ContextLink _value;

  @override
  ContextLink version(ContextLinkVersionEnum version) => call(version: version);

  @override
  ContextLink workspaceId(String? workspaceId) =>
      call(workspaceId: workspaceId);

  @override
  ContextLink payload(String payload) => call(payload: payload);

  @override
  ContextLink fragment(String fragment) => call(fragment: fragment);

  @override
  ContextLink bytes(int bytes) => call(bytes: bytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ContextLink call({
    Object? version = const $CopyWithPlaceholder(),
    Object? workspaceId = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? fragment = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
  }) {
    return ContextLink(
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as ContextLinkVersionEnum,
      workspaceId: workspaceId == const $CopyWithPlaceholder()
          ? _value.workspaceId
          // ignore: cast_nullable_to_non_nullable
          : workspaceId as String?,
      payload: payload == const $CopyWithPlaceholder() || payload == null
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as String,
      fragment: fragment == const $CopyWithPlaceholder() || fragment == null
          ? _value.fragment
          // ignore: cast_nullable_to_non_nullable
          : fragment as String,
      bytes: bytes == const $CopyWithPlaceholder() || bytes == null
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int,
    );
  }
}

extension $ContextLinkCopyWith on ContextLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContextLink.copyWith(...)` or `instanceOfContextLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContextLinkCWProxy get copyWith => _$ContextLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ContextLink _$ContextLinkFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ContextLink', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['version', 'payload', 'fragment', 'bytes'],
      );
      final val = ContextLink(
        version: $checkedConvert(
          'version',
          (v) => $enumDecode(_$ContextLinkVersionEnumEnumMap, v),
        ),
        workspaceId: $checkedConvert('workspaceId', (v) => v as String?),
        payload: $checkedConvert('payload', (v) => v as String),
        fragment: $checkedConvert('fragment', (v) => v as String),
        bytes: $checkedConvert('bytes', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ContextLinkToJson(ContextLink instance) =>
    <String, dynamic>{
      'version': _$ContextLinkVersionEnumEnumMap[instance.version]!,
      'workspaceId': ?instance.workspaceId,
      'payload': instance.payload,
      'fragment': instance.fragment,
      'bytes': instance.bytes,
    };

const _$ContextLinkVersionEnumEnumMap = {ContextLinkVersionEnum.number1: 1};
