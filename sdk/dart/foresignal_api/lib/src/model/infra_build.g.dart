// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_build.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraBuildCWProxy {
  InfraBuild id(String id);

  InfraBuild status(InfraBuildStatusEnum status);

  InfraBuild startedAt(DateTime startedAt);

  InfraBuild finishedAt(DateTime? finishedAt);

  InfraBuild rulesVersion(String rulesVersion);

  InfraBuild inputs(Map<String, int>? inputs);

  InfraBuild nodes(int? nodes);

  InfraBuild edgesByMethod(Map<String, int>? edgesByMethod);

  InfraBuild edgesByLabel(Map<String, int>? edgesByLabel);

  InfraBuild warnings(List<String>? warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraBuild(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraBuild(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraBuild call({
    String id,
    InfraBuildStatusEnum status,
    DateTime startedAt,
    DateTime? finishedAt,
    String rulesVersion,
    Map<String, int>? inputs,
    int? nodes,
    Map<String, int>? edgesByMethod,
    Map<String, int>? edgesByLabel,
    List<String>? warnings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraBuild.copyWith(...)` or call `instanceOfInfraBuild.copyWith.fieldName(value)` for a single field.
class _$InfraBuildCWProxyImpl implements _$InfraBuildCWProxy {
  const _$InfraBuildCWProxyImpl(this._value);

  final InfraBuild _value;

  @override
  InfraBuild id(String id) => call(id: id);

  @override
  InfraBuild status(InfraBuildStatusEnum status) => call(status: status);

  @override
  InfraBuild startedAt(DateTime startedAt) => call(startedAt: startedAt);

  @override
  InfraBuild finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  @override
  InfraBuild rulesVersion(String rulesVersion) =>
      call(rulesVersion: rulesVersion);

  @override
  InfraBuild inputs(Map<String, int>? inputs) => call(inputs: inputs);

  @override
  InfraBuild nodes(int? nodes) => call(nodes: nodes);

  @override
  InfraBuild edgesByMethod(Map<String, int>? edgesByMethod) =>
      call(edgesByMethod: edgesByMethod);

  @override
  InfraBuild edgesByLabel(Map<String, int>? edgesByLabel) =>
      call(edgesByLabel: edgesByLabel);

  @override
  InfraBuild warnings(List<String>? warnings) => call(warnings: warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraBuild(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraBuild(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraBuild call({
    Object? id = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? rulesVersion = const $CopyWithPlaceholder(),
    Object? inputs = const $CopyWithPlaceholder(),
    Object? nodes = const $CopyWithPlaceholder(),
    Object? edgesByMethod = const $CopyWithPlaceholder(),
    Object? edgesByLabel = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
  }) {
    return InfraBuild(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as InfraBuildStatusEnum,
      startedAt: startedAt == const $CopyWithPlaceholder() || startedAt == null
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      rulesVersion:
          rulesVersion == const $CopyWithPlaceholder() || rulesVersion == null
          ? _value.rulesVersion
          // ignore: cast_nullable_to_non_nullable
          : rulesVersion as String,
      inputs: inputs == const $CopyWithPlaceholder()
          ? _value.inputs
          // ignore: cast_nullable_to_non_nullable
          : inputs as Map<String, int>?,
      nodes: nodes == const $CopyWithPlaceholder()
          ? _value.nodes
          // ignore: cast_nullable_to_non_nullable
          : nodes as int?,
      edgesByMethod: edgesByMethod == const $CopyWithPlaceholder()
          ? _value.edgesByMethod
          // ignore: cast_nullable_to_non_nullable
          : edgesByMethod as Map<String, int>?,
      edgesByLabel: edgesByLabel == const $CopyWithPlaceholder()
          ? _value.edgesByLabel
          // ignore: cast_nullable_to_non_nullable
          : edgesByLabel as Map<String, int>?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<String>?,
    );
  }
}

extension $InfraBuildCopyWith on InfraBuild {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraBuild.copyWith(...)` or `instanceOfInfraBuild.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraBuildCWProxy get copyWith => _$InfraBuildCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraBuild _$InfraBuildFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraBuild', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'status', 'startedAt', 'rulesVersion'],
      );
      final val = InfraBuild(
        id: $checkedConvert('id', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$InfraBuildStatusEnumEnumMap, v),
        ),
        startedAt: $checkedConvert(
          'startedAt',
          (v) => DateTime.parse(v as String),
        ),
        finishedAt: $checkedConvert(
          'finishedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        rulesVersion: $checkedConvert('rulesVersion', (v) => v as String),
        inputs: $checkedConvert(
          'inputs',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        nodes: $checkedConvert('nodes', (v) => (v as num?)?.toInt()),
        edgesByMethod: $checkedConvert(
          'edgesByMethod',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        edgesByLabel: $checkedConvert(
          'edgesByLabel',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        warnings: $checkedConvert(
          'warnings',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$InfraBuildToJson(InfraBuild instance) =>
    <String, dynamic>{
      'id': instance.id,
      'status': _$InfraBuildStatusEnumEnumMap[instance.status]!,
      'startedAt': instance.startedAt.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'rulesVersion': instance.rulesVersion,
      'inputs': ?instance.inputs,
      'nodes': ?instance.nodes,
      'edgesByMethod': ?instance.edgesByMethod,
      'edgesByLabel': ?instance.edgesByLabel,
      'warnings': ?instance.warnings,
    };

const _$InfraBuildStatusEnumEnumMap = {
  InfraBuildStatusEnum.running: 'running',
  InfraBuildStatusEnum.done: 'done',
  InfraBuildStatusEnum.failed: 'failed',
};
