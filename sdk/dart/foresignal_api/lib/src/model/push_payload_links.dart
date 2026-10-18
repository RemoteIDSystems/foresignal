//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_payload_links.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushPayloadLinks {
  /// Returns a new [PushPayloadLinks] instance.
  PushPayloadLinks({

     this.api,

     this.app,

     this.web,
  });

  @JsonKey(
    
    name: r'api',
    required: false,
    includeIfNull: false,
  )


  final String? api;



  @JsonKey(
    
    name: r'app',
    required: false,
    includeIfNull: false,
  )


  final String? app;



  @JsonKey(
    
    name: r'web',
    required: false,
    includeIfNull: false,
  )


  final String? web;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushPayloadLinks &&
      other.api == api &&
      other.app == app &&
      other.web == web;

    @override
    int get hashCode =>
        api.hashCode +
        app.hashCode +
        web.hashCode;

  factory PushPayloadLinks.fromJson(Map<String, dynamic> json) => _$PushPayloadLinksFromJson(json);

  Map<String, dynamic> toJson() => _$PushPayloadLinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

