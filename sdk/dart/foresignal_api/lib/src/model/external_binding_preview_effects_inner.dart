//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_binding_preview_effects_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalBindingPreviewEffectsInner {
  /// Returns a new [ExternalBindingPreviewEffectsInner] instance.
  ExternalBindingPreviewEffectsInner({

     this.itemRef,

     this.action,

     this.targetRef,
  });

  @JsonKey(
    
    name: r'itemRef',
    required: false,
    includeIfNull: false,
  )


  final String? itemRef;



  @JsonKey(
    
    name: r'action',
    required: false,
    includeIfNull: false,
  )


  final String? action;



  @JsonKey(
    
    name: r'targetRef',
    required: false,
    includeIfNull: false,
  )


  final String? targetRef;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalBindingPreviewEffectsInner &&
      other.itemRef == itemRef &&
      other.action == action &&
      other.targetRef == targetRef;

    @override
    int get hashCode =>
        itemRef.hashCode +
        action.hashCode +
        (targetRef == null ? 0 : targetRef.hashCode);

  factory ExternalBindingPreviewEffectsInner.fromJson(Map<String, dynamic> json) => _$ExternalBindingPreviewEffectsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalBindingPreviewEffectsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

