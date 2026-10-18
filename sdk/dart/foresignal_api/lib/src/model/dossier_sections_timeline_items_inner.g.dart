// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_timeline_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsTimelineItemsInnerCWProxy {
  DossierSectionsTimelineItemsInner at(DateTime at);

  DossierSectionsTimelineItemsInner kind(
    DossierSectionsTimelineItemsInnerKindEnum kind,
  );

  DossierSectionsTimelineItemsInner ref(String? ref);

  DossierSectionsTimelineItemsInner title(String title);

  DossierSectionsTimelineItemsInner severity(String? severity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsTimelineItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsTimelineItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsTimelineItemsInner call({
    DateTime at,
    DossierSectionsTimelineItemsInnerKindEnum kind,
    String? ref,
    String title,
    String? severity,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsTimelineItemsInner.copyWith(...)` or call `instanceOfDossierSectionsTimelineItemsInner.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsTimelineItemsInnerCWProxyImpl
    implements _$DossierSectionsTimelineItemsInnerCWProxy {
  const _$DossierSectionsTimelineItemsInnerCWProxyImpl(this._value);

  final DossierSectionsTimelineItemsInner _value;

  @override
  DossierSectionsTimelineItemsInner at(DateTime at) => call(at: at);

  @override
  DossierSectionsTimelineItemsInner kind(
    DossierSectionsTimelineItemsInnerKindEnum kind,
  ) => call(kind: kind);

  @override
  DossierSectionsTimelineItemsInner ref(String? ref) => call(ref: ref);

  @override
  DossierSectionsTimelineItemsInner title(String title) => call(title: title);

  @override
  DossierSectionsTimelineItemsInner severity(String? severity) =>
      call(severity: severity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsTimelineItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsTimelineItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsTimelineItemsInner call({
    Object? at = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsTimelineItemsInner(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as DossierSectionsTimelineItemsInnerKindEnum,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as String?,
    );
  }
}

extension $DossierSectionsTimelineItemsInnerCopyWith
    on DossierSectionsTimelineItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsTimelineItemsInner.copyWith(...)` or `instanceOfDossierSectionsTimelineItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsTimelineItemsInnerCWProxy get copyWith =>
      _$DossierSectionsTimelineItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsTimelineItemsInner _$DossierSectionsTimelineItemsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsTimelineItemsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['at', 'kind', 'title']);
  final val = DossierSectionsTimelineItemsInner(
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$DossierSectionsTimelineItemsInnerKindEnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String?),
    title: $checkedConvert('title', (v) => v as String),
    severity: $checkedConvert('severity', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsTimelineItemsInnerToJson(
  DossierSectionsTimelineItemsInner instance,
) => <String, dynamic>{
  'at': instance.at.toIso8601String(),
  'kind': _$DossierSectionsTimelineItemsInnerKindEnumEnumMap[instance.kind]!,
  'ref': ?instance.ref,
  'title': instance.title,
  'severity': ?instance.severity,
};

const _$DossierSectionsTimelineItemsInnerKindEnumEnumMap = {
  DossierSectionsTimelineItemsInnerKindEnum.version: 'version',
  DossierSectionsTimelineItemsInnerKindEnum.alert: 'alert',
  DossierSectionsTimelineItemsInnerKindEnum.finding: 'finding',
  DossierSectionsTimelineItemsInnerKindEnum.situationLog: 'situation-log',
  DossierSectionsTimelineItemsInnerKindEnum.annotation: 'annotation',
  DossierSectionsTimelineItemsInnerKindEnum.other: 'other',
};
