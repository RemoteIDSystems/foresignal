// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'principal_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrincipalSummaryCWProxy {
  PrincipalSummary id(String id);

  PrincipalSummary handle(String handle);

  PrincipalSummary displayName(String displayName);

  PrincipalSummary kind(PrincipalSummaryKindEnum kind);

  PrincipalSummary avatarMediaId(String? avatarMediaId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrincipalSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrincipalSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  PrincipalSummary call({
    String id,
    String handle,
    String displayName,
    PrincipalSummaryKindEnum kind,
    String? avatarMediaId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrincipalSummary.copyWith(...)` or call `instanceOfPrincipalSummary.copyWith.fieldName(value)` for a single field.
class _$PrincipalSummaryCWProxyImpl implements _$PrincipalSummaryCWProxy {
  const _$PrincipalSummaryCWProxyImpl(this._value);

  final PrincipalSummary _value;

  @override
  PrincipalSummary id(String id) => call(id: id);

  @override
  PrincipalSummary handle(String handle) => call(handle: handle);

  @override
  PrincipalSummary displayName(String displayName) =>
      call(displayName: displayName);

  @override
  PrincipalSummary kind(PrincipalSummaryKindEnum kind) => call(kind: kind);

  @override
  PrincipalSummary avatarMediaId(String? avatarMediaId) =>
      call(avatarMediaId: avatarMediaId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrincipalSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrincipalSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrincipalSummary call({
    Object? id = const $CopyWithPlaceholder(),
    Object? handle = const $CopyWithPlaceholder(),
    Object? displayName = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? avatarMediaId = const $CopyWithPlaceholder(),
  }) {
    return PrincipalSummary(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      handle: handle == const $CopyWithPlaceholder() || handle == null
          ? _value.handle
          // ignore: cast_nullable_to_non_nullable
          : handle as String,
      displayName:
          displayName == const $CopyWithPlaceholder() || displayName == null
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as PrincipalSummaryKindEnum,
      avatarMediaId: avatarMediaId == const $CopyWithPlaceholder()
          ? _value.avatarMediaId
          // ignore: cast_nullable_to_non_nullable
          : avatarMediaId as String?,
    );
  }
}

extension $PrincipalSummaryCopyWith on PrincipalSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrincipalSummary.copyWith(...)` or `instanceOfPrincipalSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrincipalSummaryCWProxy get copyWith => _$PrincipalSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrincipalSummary _$PrincipalSummaryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrincipalSummary', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'handle', 'displayName', 'kind'],
      );
      final val = PrincipalSummary(
        id: $checkedConvert('id', (v) => v as String),
        handle: $checkedConvert('handle', (v) => v as String),
        displayName: $checkedConvert('displayName', (v) => v as String),
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$PrincipalSummaryKindEnumEnumMap, v),
        ),
        avatarMediaId: $checkedConvert('avatarMediaId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PrincipalSummaryToJson(PrincipalSummary instance) =>
    <String, dynamic>{
      'id': instance.id,
      'handle': instance.handle,
      'displayName': instance.displayName,
      'kind': _$PrincipalSummaryKindEnumEnumMap[instance.kind]!,
      'avatarMediaId': ?instance.avatarMediaId,
    };

const _$PrincipalSummaryKindEnumEnumMap = {
  PrincipalSummaryKindEnum.person: 'person',
  PrincipalSummaryKindEnum.agent: 'agent',
  PrincipalSummaryKindEnum.service: 'service',
};
