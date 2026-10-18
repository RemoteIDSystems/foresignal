// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'context_patch_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContextPatchEventCWProxy {
  ContextPatchEvent workspaceId(String workspaceId);

  ContextPatchEvent version(int version);

  ContextPatchEvent patch_(Map<String, Object> patch_);

  ContextPatchEvent originClientId(String? originClientId);

  ContextPatchEvent principal(String? principal);

  ContextPatchEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextPatchEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextPatchEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ContextPatchEvent call({
    String workspaceId,
    int version,
    Map<String, Object> patch_,
    String? originClientId,
    String? principal,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContextPatchEvent.copyWith(...)` or call `instanceOfContextPatchEvent.copyWith.fieldName(value)` for a single field.
class _$ContextPatchEventCWProxyImpl implements _$ContextPatchEventCWProxy {
  const _$ContextPatchEventCWProxyImpl(this._value);

  final ContextPatchEvent _value;

  @override
  ContextPatchEvent workspaceId(String workspaceId) =>
      call(workspaceId: workspaceId);

  @override
  ContextPatchEvent version(int version) => call(version: version);

  @override
  ContextPatchEvent patch_(Map<String, Object> patch_) => call(patch_: patch_);

  @override
  ContextPatchEvent originClientId(String? originClientId) =>
      call(originClientId: originClientId);

  @override
  ContextPatchEvent principal(String? principal) => call(principal: principal);

  @override
  ContextPatchEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextPatchEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextPatchEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ContextPatchEvent call({
    Object? workspaceId = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? patch_ = const $CopyWithPlaceholder(),
    Object? originClientId = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ContextPatchEvent(
      workspaceId:
          workspaceId == const $CopyWithPlaceholder() || workspaceId == null
          ? _value.workspaceId
          // ignore: cast_nullable_to_non_nullable
          : workspaceId as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      patch_: patch_ == const $CopyWithPlaceholder() || patch_ == null
          ? _value.patch_
          // ignore: cast_nullable_to_non_nullable
          : patch_ as Map<String, Object>,
      originClientId: originClientId == const $CopyWithPlaceholder()
          ? _value.originClientId
          // ignore: cast_nullable_to_non_nullable
          : originClientId as String?,
      principal: principal == const $CopyWithPlaceholder()
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ContextPatchEventCopyWith on ContextPatchEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContextPatchEvent.copyWith(...)` or `instanceOfContextPatchEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContextPatchEventCWProxy get copyWith =>
      _$ContextPatchEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ContextPatchEvent _$ContextPatchEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ContextPatchEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['workspaceId', 'version', 'patch', 'at'],
      );
      final val = ContextPatchEvent(
        workspaceId: $checkedConvert('workspaceId', (v) => v as String),
        version: $checkedConvert('version', (v) => (v as num).toInt()),
        patch_: $checkedConvert(
          'patch',
          (v) => (v as Map<String, dynamic>).map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        originClientId: $checkedConvert('originClientId', (v) => v as String?),
        principal: $checkedConvert('principal', (v) => v as String?),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    }, fieldKeyMap: const {'patch_': 'patch'});

Map<String, dynamic> _$ContextPatchEventToJson(ContextPatchEvent instance) =>
    <String, dynamic>{
      'workspaceId': instance.workspaceId,
      'version': instance.version,
      'patch': instance.patch_,
      'originClientId': ?instance.originClientId,
      'principal': ?instance.principal,
      'at': instance.at.toIso8601String(),
    };
